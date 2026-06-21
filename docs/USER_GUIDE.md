# Guide utilisateur — SMS Forwarder

## Table des matieres

1. [Qu'est-ce que SMS Forwarder ?](#1-quest-ce-que-sms-forwarder-)
2. [Installation](#2-installation)
3. [Premier lancement](#3-premier-lancement)
4. [Configurer le numero de destination](#4-configurer-le-numero-de-destination)
5. [Activer le transfert](#5-activer-le-transfert)
6. [Dashboard temps reel](#6-dashboard-temps-reel)
7. [Regles de transfert](#7-regles-de-transfert)
8. [Webhook HTTP](#8-webhook-http)
9. [Surveillance d'apps tierces](#9-surveillance-dapps-tierces)
10. [Consulter l'historique](#10-consulter-lhistorique)
11. [Centre de notifications](#11-centre-de-notifications)
12. [Diagnostics](#12-diagnostics)
13. [Configurer les filtres](#13-configurer-les-filtres)
14. [Politique de retry](#14-politique-de-retry)
15. [Changer la langue](#15-changer-la-langue)
16. [Statistiques](#16-statistiques)
17. [Widget Android](#17-widget-android)
18. [Multi-SIM](#18-multi-sim)
19. [FAQ](#19-faq)

---

## 1. Qu'est-ce que SMS Forwarder ?

SMS Forwarder transfere automatiquement chaque SMS, message RCS et notification d'application que vous recevez vers un numero de telephone ou un webhook HTTP de votre choix. L'application fonctionne en arriere-plan, sans intervention de votre part, et conserve un historique complet de tous les transferts.

L'application est entierement bilingue (Francais / Anglais) et detecte automatiquement la langue de votre telephone. Toutes les donnees restent sur votre appareil : rien n'est envoye a un serveur externe (sauf si vous configurez un webhook).

---

## 2. Installation

### Telecharger l'APK

1. Recuperez le fichier APK depuis la [page Releases](https://github.com/ronylicha/SMS-Forwarder/releases) de GitHub.
2. Transferez-le sur votre telephone (par cable USB, email ou Bluetooth).

### Google Play

L'application estgalement disponible sur le [Google Play Store](https://play.google.com/store/apps/details?id=com.qrcommunication.smsforwarder).

### Autoriser les sources inconnues

Avant d'installer un fichier APK qui ne vient pas du Google Play Store, Android vous demande d'autoriser l'installation depuis des sources inconnues.

1. Ouvrez **Parametres** sur votre telephone.
2. Allez dans **Applications** (ou **Gestion des applications**).
3. Appuyez sur les trois points en haut a droite, puis **Acces special**.
4. Appuyez sur **Installer des applications inconnues**.
5. Selectionnez le gestionnaire de fichiers ou l'application que vous utilisez pour ouvrir l'APK, et activez l'option.

### Installer l'application

1. Ouvrez le fichier APK avec votre gestionnaire de fichiers.
2. Appuyez sur **Installer**.
3. Une fois l'installation terminee, appuyez sur **Ouvrir**.

---

## 3. Premier lancement

Au premier lancement, un onboarding en 4 etapes vous guide :

1. **Bienvenue** — presentation des fonctionnalites principales
2. **Permissions** — chaque permission est expliquee individuellement
3. **Numero de destination** — configuration du numero cible
4. **SMS de test** — envoi d'un message de validation

Un avertissement de securite vous rappelle que les SMS transitent en clair via le reseau de votre operateur.

### Permissions a accepter

SMS Forwarder a besoin de trois permissions pour fonctionner :

| Permission | A quoi elle sert |
|---|---|
| **Reception SMS** | Detecter les SMS entrants pour les transferer |
| **Envoi SMS** | Envoyer les SMS vers le numero de destination |
| **Notifications** | Afficher le service actif en arriere-plan |

### Acces aux notifications (pour les messages RCS)

Pour que l'application puisse egalement capturer les messages RCS (Google Messages, Samsung Messages) :

1. Dans l'application, appuyez sur **Reglages**.
2. Dans la section **Acces aux notifications**, appuyez sur **Configurer l'acces**.
3. Android ouvre les parametres systeme : activez **SMS Forwarder** dans la liste.
4. Revenez dans l'application. La mention **Acces active** confirme la configuration.

> Cette etape est necessaire ealement si vous souhaitez surveiller les notifications d'apps tierces (WhatsApp, Telegram...).

---

## 4. Configurer le numero de destination

1. Depuis le dashboard, appuyez sur **Reglages**.
2. Dans la section **Numero de destination**, saisissez le numero au format international : `+33 6 12 34 56 78`.
3. Appuyez sur **Enregistrer**.
4. Un **toast de confirmation** "Numero enregistre" apparait en bas de l'ecran.

### Tester la configuration

1. Dans **Reglages > Numero de destination**, appuyez sur **Envoyer un test**.
2. Un SMS `[SMS Forwarder] This is a test SMS.` est envoye au numero configure.
3. Un toast confirme le succes ou l'echec de l'envoi.

---

## 5. Activer le transfert

1. Appuyez sur le **bouton bascule** au centre du dashboard.
2. La carte passe en mode actif : **Transfert actif**, numero de destination affiche.
3. Une notification permanente apparait indiquant que **SMS Forwarder est actif**.

Pour desactiver le transfert, appuyez a nouveau sur le meme bouton bascule.

---

## 6. Dashboard temps reel

Le dashboard affiche des statistiques en temps reel qui s'actualisent automatiquement :

| Element | Description |
|---|---|
| **Envoyes 24h** | Nombre de SMS transferes avec succes dans les dernieres 24h |
| **Echoues 24h** | Nombre d'echecs dans les dernieres 24h |
| **Total transferes** | Compteur global depuis l'installation |
| **Taux de succes** | Pourcentage de reussite global |
| **Badge notifications** | Nombre de notifications non lues (point rouge) |

Le dashboard propose un acces rapide vers : Historique, Regles, Diagnostics, Statistiques et Reglages.

---

## 7. Regles de transfert

Les regles permettent de router differemment les messages selon l'expediteur et le contenu. Chaque regle peut envoyer vers un numero SMS ou un webhook different.

Acces : **Reglages > Avance > Regles de transfert** ou depuis le dashboard.

### Creer une regle

1. Appuyez sur **Nouvelle regle**.
2. **Nom** : nom descriptif (ex: "Codes 2FA vers webhook").
3. **Pattern expediteur (regex, optionnel)** : filtre par numero, ex: `^(\+33|0)6`.
4. **Mot-cle / regex contenu (optionnel)** : filtre le texte, ex: `code|otp|verification`.
5. **Destination** : numero SMS ou URL de webhook.
6. **Active** : cochez pour activer immediatement.
7. Appuyez sur **Enregistrer**. Un toast "Regle enregistre apparait.

### Tester une regle

Avant d'activer une regle, vous pouvez la tester avec un SMS d'exemple :

1. Dans l'edition de regle, section test.
2. Saisissez un **expediteur de test** et un **contenu de test**.
3. Appuyez sur **Tester**.
4. Le resultat indique si la regle correspond.

> Une regle sans aucun pattern correspond a TOUS les SMS.

### Priorite

Les regles sont evaluees par ordre de priorite. La premiere regle qui correspond determine la destination. Si aucune regle ne correspond, le fallback utilise la destination globale.

---

## 8. Webhook HTTP

Le webhook permet d'envoyer chaque message en JSON vers n'importe quel endpoint HTTP.

### Format du payload

```json
POST https://votre-endpoint.com/sms
Content-Type: application/json

{
  "sender": "+336****5678",
  "content": "Votre code: 8472",
  "receivedAt": "2026-05-01T19:08:32Z",
  "sourceLabel": "WhatsApp",
  "originalDestination": "+336****0000"
}
```

| Champ | Type | Description |
|---|---|---|
| `sender` | String | Numero de l'expediteur |
| `content` | String | Corps complet du message |
| `receivedAt` | String (ISO 8601) | Timestamp de reception |
| `sourceLabel` | String? | Nom de l'app d'origine (si notification d'app tierce) |
| `originalDestination` | String? | Numero de destination global configure |

### Configuration

1. Creez une regle de transfert avec le type de destination **Webhook**.
2. Saisissez l'URL complete de votre endpoint.
3. Testez avec le bouton **Tester**.

> Le webhook utilise `HttpURLConnection` natif (aucune dependance externe). En cas d'echec reseau, le retry automatique s'applique selon votre politique configuree.

---

## 9. Surveillance d'apps tierces

SMS Forwarder peut intercepter les notifications d'applications comme WhatsApp, Telegram, Allo, Ringover ou Onoff, et les transferer comme des SMS ou via webhook.

### Activer la surveillance

1. Allez dans **Reglages > Applications tierces**.
2. Activez la **Surveillance des notifications**.
3. Appuyez sur **Gerer les applications**.
4. Ajoutez les apps dont vous souhaitez transferer les notifications.

Le champ `sourceLabel` dans le payload webhook indique l'application d'origine.

---

## 10. Consulter l'historique

L'historique liste tous les messages recus et le resultat de leur transfert.

### Signification des statuts

| Statut | Couleur | Signification |
|---|---|---|
| **Envoye** | Vert | Le message a ete transfere avec succes |
| **Echoue** | Rouge | Le transfert a echoue (reseau indisponible, etc.) |
| **En attente** | Bleu | Le transfert est en cours ou en file d'attente |
| **Filtre** | Gris | Le message a ete bloque par une regle de filtrage |

### Filtres disponibles

- **Recherche texte** : par expediteur, contenu ou destination
- **Filtre par statut** : Tous, Envoyes, Echoues, En attente, Filtres
- **Filtre par plage de dates** : DateRangePicker Material 3
- **Renvoyer** : bouton inline sur les SMS echoues

---

## 11. Centre de notifications

Le centre de notifications rassemble les alertes systeme de l'application :

- **Erreurs de regles** : regex invalide, destination manquante
- **Destination injoignable** : webhook ou SMS en echec repetite
- **Alertes batterie** : optimisation batterie desactivee
- **Quota** : alertes de forfait

Appuyez sur le badge du dashboard ou sur **Reglages > Centre de notifications** pour consulter. Marquage individuel ou global comme lu.

---

## 12. Diagnostics

L'ecran Diagnostics audite l'etat du systeme :

| Check | Description |
|---|---|
| **Permissions** | Verifie que toutes les permissions requises sont accordees |
| **Optimisation batterie** | controle si l'app est exclue du Doze mode |
| **Acces aux notifications** | Verifie l'acces NotificationListener (RCS + apps tierces) |
| **Connectivite reseau** | pour les regles webhook |

Chaque check propose un **bouton direct** vers les parametres systeme correspondants.

---

## 13. Configurer les filtres

Les filtres controlent quels SMS sont transferes globalement (independamment des regles de transfert).

Acces : **Reglages > Filtrage**.

| Mode | Comportement |
|---|---|
| **Aucun** | Tous les SMS sont transferes |
| **Liste blanche** | Seuls les SMS correspondant aux regles sont transferes |
| **Liste noire** | Les SMS correspondant aux regles sont bloques |

---

## 14. Politique de retry

La politique de retry est configurable dans **Reglages > Politique de retry** :

| Parametre | Options |
|---|---|
| **Tentatives max** | 1 a 10 (slider) |
| **Delai initial** | 30s, 1min, 5min, 15min |
| **Multiplicateur backoff** | x1.5, x2, x3 |

En cas d'echec d'envoi, l'app retente automatiquement selon cette politique.

---

## 15. Changer la langue

L'application est bilingue FR/EN avec detection automatique.

1. Allez dans **Reglages > Langue**.
2. Choisissez parmi :
   - **Suivre le systeme** : FR si le telephone est en francais, EN sinon
   - **Francais** : force le francais
   - **English** : force l'anglais
3. L'application redemarre immediatement avec la nouvelle langue.
4. Un toast "Langue modifiee" confirme le changement.

---

## 16. Statistiques

L'ecran Statistiques affiche :

- **Resume** : total, envoyes, echoues, filtres, en attente, taux de succes
- **Graphique par jour** : activite quotidienne sur 7j / 14j / 30j
- **Export CSV** : export de l'historique complet

---

## 17. Widget Android

Le widget permet d'activer/desactiver le transfert depuis l'ecran d'accueil :

1. Appuyez longuement sur une zone vide de l'ecran d'accueil.
2. Appuyez sur **Widgets**.
3. Recherchez **SMS Forwarder**.
4. Glissez le widget a l'emplacement souhaite.

Le widget affiche ON/OFF et le compteur de SMS transferes.

---

## 18. Multi-SIM

Si votre telephone a deux cartes SIM :

- **SIM de reception** : quelles SIMs sont surveillees (Toutes / SIM 1 / SIM 2)
- **SIM d'envoi** : quelle SIM utilisee pour envoyer les SMS transferes

Configuration dans **Reglages > Multi-SIM** (apparait automatiquement si 2 SIMs detectees).

---

## 19. FAQ

Consultez le fichier [FAQ.md](FAQ.md) pour les questions frequentes detaillees.

**Questions rapides :**

- **Le transfert fonctionne-t-il si j'ai ferme l'application ?** Oui. Le Foreground Service reste actif tant que la notification persistante est visible.
- **L'application redemarrerait-elle apres un reboot ?** Oui, automatiquement grace au BootReceiver.
- **Comment eviter une boucle ?** L'app detecte les messages qu'elle a elle-meme envoyes + le numero de destination est en liste noire automatique.
- **Le webhook fonctionne-t-il hors ligne ?** Non, il faut une connexion data. Le retry automatique s'applique en cas d'echec.
- **Les SMS transferes me coutent-ils de l'argent ?** Chaque SMS transfere consomme un SMS de votre forfait. Le transfert via webhook est gratuit (data).
- **Puis-je changer la langue ?** Oui, dans Reglages > Langue (Systeme / Francais / English).
