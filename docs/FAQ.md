# FAQ — SMS Forwarder

🇫🇷 Français | 🇬🇧 [English](FAQ_EN.md)

Questions frequentes sur l'utilisation de SMS Forwarder.

---

## L'application consomme-t-elle beaucoup de batterie ?

SMS Forwarder fonctionne via un Foreground Service (visible par sa notification permanente). Ce type de service est concu pour avoir une empreinte reduite : il ne fait rien tant qu'aucun message n'arrive.

Dans la pratique, la consommation est negligeable. Si votre telephone est restrictif sur la batterie (Xiaomi, Huawei, Samsung), excluez SMS Forwarder de l'optimisation batterie :

**Parametres > Applications > SMS Forwarder > Batterie > Non optimisee**

Vous pouvez verifier l'etat dans l'ecran **Diagnostics** de l'application.

---

## Les SMS transferes me coutent-ils de l'argent ?

Chaque transfert SMS envoie un vrai SMS via le reseau de votre operateur. Si votre forfait inclut des SMS illimites, il n'y aura aucun cout supplementaire.

**Le transfert via webhook HTTP est en revanche gratuit** (utilise votre connexion data).

Les SMS de plus de 160 caracteres sont decoupes en plusieurs parties par le reseau GSM et peuvent compter pour plusieurs SMS.

---

## Mes donnees sont-elles envoyees a un serveur ?

Non. SMS Forwarder ne communique avec aucun serveur externe par defaut. Toutes vos donnees sont stockees exclusivement sur votre appareil.

**Exception** : si vous configurez un webhook, les messages correspondant a cette regle sont envoyes uniquement vers l'URL que vous avez definie. Vous gardez le controle total. Le code source est ouvert et verifiable sur GitHub.

---

## L'application fonctionne-t-elle avec les messages RCS ?

Oui. Les messages RCS sont captures via deux mecanismes complementaires :

- **ContentObserver** : surveille la boite de reception SMS/RCS du systeme
- **NotificationListener** : intercepte les notifications de Google Messages, Samsung Messages et AOSP Messages

Activez l'acces aux notifications dans **Reglages > Acces aux notifications > Configurer l'acces**.

---

## Puis-je transferer les notifications WhatsApp, Telegram, etc. ?

Oui. SMS Forwarder peut surveiller les notifications d'applications tierces et les transferer comme des SMS ou via webhook.

1. Allez dans **Reglages > Applications tierces**.
2. Activez la surveillance.
3. Ajoutez les apps souhaitees (WhatsApp, Telegram, Allo, Ringover, Onoff...).

Le champ `sourceLabel` indique l'app d'origine dans le payload webhook.

---

## Comment fonctionnent les regles de transfert ?

Chaque message entrant est analyse par vos regles, dans l'ordre de priorite :

1. **Pattern expediteur (regex)** : filtre par numero ou motif
2. **Mot-cle (regex)** : condition sur le contenu
3. **Destination** : SMS ou webhook, specifique a cette regle

Si une regle correspond, le message est envoye vers sa destination. Si aucune regle ne correspond, le fallback utilise la destination globale.

Vous pouvez tester chaque regle interactivement avant de l'activer.

---

## Que se passe-t-il en cas d'echec d'envoi ?

L'application retente automatiquement selon votre **politique de retry configurable** :

- **Tentatives max** : 1 a 10
- **Delai initial** : 30s, 1min, 5min ou 15min
- **Backoff exponentiel** : x1.5, x2 ou x3

Au-dela du nombre de tentatives, le message passe en statut **Echoue** et vous pouvez le renvoyer manuellement depuis l'historique.

---

## Le webhook fonctionne-t-il sans connexion internet ?

Non. Le webhook necessite une connexion data. En cas d'echec (reseau indisponible, endpoint injoignable), l'app retente selon votre politique de retry. Le centre de notifications vous alerte des erreurs repetees.

---

## Comment eviter une boucle de transfert ?

SMS Forwarder detecte automatiquement les messages qu'elle a elle-meme envoyes et les exclut. De plus, le numero de destination est automatiquement ajoute a la liste noire. Il est impossible de creer une boucle infinie.

---

## L'application fonctionne-t-elle apres un redemarrage du telephone ?

Oui. Au redemarrage, le BootReceiver relance automatiquement le service si le transfert etait actif. Votre configuration, vos regles et l'etat d'activation sont conserves.

---

## Puis-je changer la langue de l'application ?

Oui. L'application est bilingue FR/EN avec detection automatique (FR si le telephone est en francais, EN sinon).

**Reglages > Langue** : Suivre le systeme / Francais / English. Le changement est immediat (redemarrage de l'activity).

---

## Puis-je filtrer les SMS par expediteur ?

Oui, de deux facons :

1. **Filtres globaux** (Reglages > Filtrage) : liste blanche / liste noire par numero ou mot-cle
2. **Regles de transfert** (Reglages > Regles de transfert) : routing avance avec regex, destination par regle et test interactif

---

## Comment exporter mon historique ?

1. Ouvrez **Statistiques** ou **Historique**.
2. Appuyez sur **Exporter CSV**.
3. Choisissez l'emplacement de sauvegarde.

Le fichier contient pour chaque message : expediteur, contenu, date de reception, date de transfert, statut et nombre de tentatives.

---

## Quelle version d'Android est necessaire ?

**Android 8.0 (Oreo)** minimum (API 26). Le design Material You est disponible sur Android 12+.

---

## L'application est-elle disponible en open source ?

Oui, sous licence **AGPL-3.0**. Le code source est integralment disponible sur [GitHub](https://github.com/ronylicha/SMS-Forwarder). Vous pouvez l'auditer, le modifier et le redistribuer.

---

## Comment desinstaller proprement ?

1. Appuyez longuement sur l'icone SMS Forwarder.
2. **Desinstaller**.
3. Confirmez.

Toutes les donnees sont supprimees. Exportez votre historique en CSV au prealable si vous souhaitez le conserver.
