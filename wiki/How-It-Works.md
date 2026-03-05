# Comment ça fonctionne

Cette page explique le mécanisme interne de SMS Forwarder sans jargon technique excessif. Elle est utile pour comprendre pourquoi certaines permissions sont nécessaires et comment l'application se comporte dans des cas particuliers.

---

## Vue d'ensemble

SMS Forwarder tourne en permanence en arrière-plan sous la forme d'un **service Android persistant**. Dès qu'un message arrive sur votre appareil — qu'il soit SMS classique ou RCS — le service le détecte, vérifie qu'il doit bien être transféré, formate un nouveau SMS et l'envoie au numéro de destination que vous avez configuré.

Le message reçu sur le téléphone destination ressemble à ceci :

```
[De: +33612345678 | 05/03/2026 14:23] Votre code est 847291
```

---

## Les 3 sources de capture

Android expose les messages entrants par plusieurs canaux selon leur type. SMS Forwarder surveille les trois simultanément.

```
Source 1 : SMS classique
    Android diffuse un événement "SMS reçu" (broadcast)
    --> SmsReceiver le capte immédiatement
    --> Fonctionne pour tous les SMS traditionnels

Source 2 : RCS via base de données
    L'application de messagerie écrit dans la base de données SMS du système
    --> SmsContentObserver surveille content://sms/inbox en continu
    --> Détecte les nouveaux messages par comparaison d'identifiants
    --> Fonctionne pour Google Messages, Samsung Messages et AOSP Messages

Source 3 : RCS via notifications
    L'application de messagerie affiche une notification de nouveau message
    --> NotificationInterceptorService lit le titre et le contenu
    --> Nécessite l'activation de l'accès aux notifications
    --> Filet de sécurité pour les RCS non détectés par les sources 1 et 2
```

Pourquoi trois sources ? Les messages RCS n'utilisent pas le système SMS standard d'Android. Selon l'application de messagerie installée et la version d'Android, certains RCS passent par la base de données, d'autres uniquement par les notifications. La triple capture garantit qu'aucun message ne passe au travers.

---

## La déduplication

Parce que les trois sources peuvent détecter le même message simultanément, chaque message passe par un filtre anti-doublons avant d'être traité.

**Mécanisme :**

1. Pour chaque message entrant, on calcule une empreinte à partir de : l'expéditeur + les 100 premiers caractères du contenu + l'horodatage arrondi à 5 secondes
2. Si cette empreinte a déjà été vue dans la dernière minute, le message est ignoré
3. Sinon, le message est traité et l'empreinte est mise en cache

La fenêtre de 5 secondes permet d'absorber les légères différences d'horodatage entre sources. Le cache est nettoyé automatiquement pour ne pas dépasser 500 entrées.

---

## Le pipeline de traitement

Une fois qu'un message passe la déduplication, il traverse le pipeline suivant dans l'ordre :

```
Message entrant
    |
    v
[1] Le transfert est-il activé ?
    Non --> Ignoré
    |
    v
[2] Un numéro de destination est-il configuré ?
    Non --> Ignoré
    |
    v
[3] Anti-boucle : l'expéditeur est-il le numéro de destination ?
    Oui --> Ignoré (évite la boucle infinie)
    |
    v
[4] Filtres actifs (liste blanche / liste noire) ?
    Message bloqué --> Enregistré avec statut "Filtré", ignoré
    |
    v
[5] Enregistrement en base de données avec statut "En attente"
    |
    v
[6] Formatage : "[De: <expéditeur> | <date>] <contenu>"
    |
    v
[7] Envoi SMS vers le numéro de destination
    |
    +-- Succès --> Statut "Envoyé", compteur incrémenté
    |
    +-- Échec  --> Statut "Échoué", déclenchement du retry
```

---

## La protection anti-boucle

Sans protection, si A transfère vers B et que B transfère aussi vers A, les deux téléphones s'enverraient des SMS indéfiniment.

SMS Forwarder détecte ce cas en comparant le numéro de l'expéditeur du message reçu avec :
- Le numéro de destination configuré
- Les numéros des cartes SIM présentes dans l'appareil

Les numéros sont normalisés avant comparaison (format E.164 `+33...`) pour que `0612345678`, `+33612345678` et `0033612345678` soient reconnus comme identiques.

---

## Le retry automatique

Si l'envoi SMS échoue (réseau indisponible, quota opérateur dépassé, etc.), l'application réessaie automatiquement jusqu'à 3 fois avec un délai croissant entre chaque tentative :

```
Tentative 1 : immédiate (à la réception)
Tentative 2 : 2 secondes après l'échec
Tentative 3 : 4 secondes après l'échec (2² s)
Tentative 4 : 8 secondes après l'échec (2³ s)

Après 3 échecs : statut définitif "Échoué"
```

En cas d'échec définitif, l'écran de détail du message permet une **retransmission manuelle** d'un seul appui.

---

## La gestion des SMS longs

Un SMS standard est limité à 160 caractères. Le message formaté `[De: <expéditeur> | <date>] <contenu>` peut dépasser cette limite pour des messages longs.

Dans ce cas, l'application utilise la fonctionnalité SMS multi-parties d'Android : le message est découpé automatiquement en segments de 153 caractères (les 7 caractères restants servent à l'en-tête de recomposition), envoyés séquentiellement. Le téléphone destinataire recompose automatiquement le message complet.

---

## Le Foreground Service et la persistance

Pour fonctionner en permanence, l'application tourne comme un **Foreground Service** Android. Ce type de service présente une notification visible en permanence dans la barre de notification (c'est une contrainte Android, pas un choix de l'application) et ne peut pas être tué silencieusement par le système pour libérer de la mémoire.

La notification affiche le nombre de SMS transférés depuis le dernier démarrage.

**Après un redémarrage de l'appareil :** un `BootReceiver` est déclenché automatiquement par Android au démarrage du système. Il relance le service si le transfert était activé avant l'extinction.

---

## Prochaine étape

Configurez les filtres et les options avancées dans [Configuration](Configuration).
