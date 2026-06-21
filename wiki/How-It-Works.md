# Comment ça fonctionne

Cette page explique le mécanisme interne de SMS Forwarder sans jargon technique excessif. Elle est utile pour comprendre pourquoi certaines permissions sont nécessaires et comment l'application se comporte dans des cas particuliers.

---

## Vue d'ensemble

SMS Forwarder tourne en permanence en arrière-plan sous la forme d'un **service Android persistant**. Dès qu'un message arrive sur votre appareil — qu'il soit SMS classique, RCS ou notification d'une application tierce — le service le détecte, vérifie qu'il doit bien être transféré, détermine sa **destination** (SMS ou webhook) et l'envoie.

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

Source 3 : RCS + apps tierces via notifications
    L'application de messagerie (ou une app tierce) affiche une notification
    --> NotificationInterceptorService lit le titre et le contenu
    --> Nécessite l'activation de l'accès aux notifications
    --> Filet de sécurité pour les RCS non détectés par les sources 1 et 2
    --> Depuis v1.3.0 : capture aussi les apps tierces (WhatsApp, Telegram…)
        si elles sont dans la whitelist
```

Pourquoi trois sources ? Les messages RCS n'utilisent pas le système SMS standard d'Android. Selon l'application de messagerie installée et la version d'Android, certains RCS passent par la base de données, d'autres uniquement par les notifications. La triple capture garantit qu'aucun message ne passe au travers.

### Surveillance d'apps tierces (v1.3.0)

En plus des SMS et RCS, le `NotificationListenerService` peut surveiller les **notifications d'applications tierces**. Seules les apps présentes dans la **whitelist** configurée par l'utilisateur sont transférées. Le `sourceLabel` (nom de l'app) est alors attaché au message et transmis au webhook.

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
Message entrant (SMS / RCS / app tierce)
    |
    v
[1] Le transfert est-il activé ?
    Non --> Ignoré
    |
    v
[2] Un numéro de destination global est-il configuré ?
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
[6] Routage : MatchForwardingRuleUseCase évalue les règles actives
    |
    +-- Une règle match --> destination = règle (SMS ou webhook)
    |
    +-- Aucune règle   --> destination = destination globale (SMS)
    |
    v
[7] DestinationDispatcher envoie vers la destination choisie
    |
    +-- SMS      --> Formatage "[De: ... | date] contenu" + SmsManager
    |
    +-- Webhook  --> POST JSON {sender, content, receivedAt, ...}
    |
    +-- Succès --> Statut "Envoyé", compteur incrémenté
    |
    +-- Échec  --> Statut "Échoué", déclenchement du retry
```

### Les règles de transfert (v1.3.0)

Les règles sont des entités `ForwardingRule` (distinctes des `FilterRule` de filtrage). Chacune comporte :

- un **critère expéditeur** (regex sur le numéro normalisé)
- un **mot-clé** (recherche dans le contenu)
- une **destination** (numéro SMS ou URL webhook)
- une **priorité** (ordre d'évaluation)
- un drapeau **activé/désactivé**

`MatchForwardingRuleUseCase` évalue les règles actives par ordre de priorité. La première règle correspondante l'emporte. Sans correspondance, le message suit la destination globale — d'où la **rétro-compatibilité** avec les versions antérieures.

### Le `DestinationDispatcher`

Ce composant route le message vers le bon canal :

| Type de destination | Action |
|---|---|
| **SMS** | Formatage `[De: <expéditeur> \| <date>] <contenu>` puis envoi via `SmsManager` |
| **Webhook** | Envoi d'un POST HTTP JSON via `HttpURLConnection` vers l'URL configurée |

---

## La protection anti-boucle

Sans protection, si A transfère vers B et que B transfère aussi vers A, les deux téléphones s'enverraient des SMS indéfiniment.

SMS Forwarder détecte ce cas en comparant le numéro de l'expéditeur du message reçu avec :
- Le numéro de destination configuré
- Les numéros des cartes SIM présentes dans l'appareil

Les numéros sont normalisés avant comparaison (format E.164 `+33...`) pour que `0612345678`, `+33612345678` et `0033612345678` soient reconnus comme identiques.

---

## Le retry automatique

Si l'envoi échoue (réseau indisponible, quota opérateur dépassé, webhook injoignable…), l'application réessaie automatiquement. Depuis v1.3.0, la **politique de retry est configurable** :

| Paramètre | Plage | Défaut |
|---|---|---|
| Tentatives max | 1 – 10 | 3 |
| Délai initial | 30s / 1min / 5min / 15min | 30s |
| Multiplicateur backoff | x1.5 / x2 / x3 | x2 |

Exemple avec les valeurs par défaut :

```
Tentative 1 : immédiate (à la réception)
Tentative 2 : 30s après l'échec
Tentative 3 : 60s après l'échec (30s x 2)
...

Après épuisement des tentatives : statut définitif "Échoué"
```

En cas d'échec définitif, l'écran de détail du message permet une **retransmission manuelle** d'un seul appui (et depuis l'historique via le bouton Renvoyer inline).

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

## Architecture (résumé)

Depuis v1.3.0, l'architecture distingue clairement le **filtrage** du **routage** :

```
[Capture] SmsReceiver / SmsContentObserver / NotificationInterceptorService
    |
    v
[Déduplication] DeduplicationCache
    |
    v
[Filtrage] FilterRule (liste blanche / liste noire) --> "Filtré" si bloqué
    |
    v
[Routage] MatchForwardingRuleUseCase --> ForwardingRule match ?
    |                                            |
    |   oui                                      |   non
    v                                            v
destination = règle                    destination = destination globale
    |
    v
[Dispatch] DestinationDispatcher --> SmsManager  OU  WebhookSender
```

Le **centre de notifications** (v1.3.0) enregistre les alertes (erreur de règle, destination injoignable, quota, batterie) en base pour un suivi depuis le Dashboard.

---

## Prochaine étape

Configurez les filtres, règles de transfert et options avancées dans [Configuration](Configuration).
