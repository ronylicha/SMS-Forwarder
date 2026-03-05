# Guide utilisateur — SMS Forwarder

## Table des matieres

1. [Qu'est-ce que SMS Forwarder ?](#1-quest-ce-que-sms-forwarder-)
2. [Installation](#2-installation)
3. [Premier lancement](#3-premier-lancement)
4. [Configurer le numero de destination](#4-configurer-le-numero-de-destination)
5. [Activer le transfert](#5-activer-le-transfert)
6. [Consulter l'historique](#6-consulter-lhistorique)
7. [Retransmettre un SMS echoue](#7-retransmettre-un-sms-echoue)
8. [Configurer les filtres](#8-configurer-les-filtres)
9. [Statistiques](#9-statistiques)
10. [Widget Android](#10-widget-android)
11. [Multi-SIM](#11-multi-sim)
12. [FAQ](#12-faq)

---

## 1. Qu'est-ce que SMS Forwarder ?

SMS Forwarder transfere automatiquement chaque SMS (et message RCS) que vous recevez vers un autre numero de telephone de votre choix. L'application fonctionne en arriere-plan, sans intervention de votre part, et conserve un historique complet de tous les transferts. Toutes les donnees restent sur votre appareil : rien n'est envoye a un serveur externe.

---

## 2. Installation

### Telecharger l'APK

1. Recuperez le fichier `SMS-Forwarder-v1.0.0.apk` fourni.
2. Transferez-le sur votre telephone (par cable USB, email ou Bluetooth).

### Autoriser les sources inconnues

Avant d'installer un fichier APK qui ne vient pas du Google Play Store, Android vous demande d'autoriser l'installation depuis des sources inconnues.

1. Ouvrez **Parametres** sur votre telephone.
2. Allez dans **Applications** (ou **Gestion des applications**).
3. Appuyez sur les trois points en haut a droite, puis **Acces special**.
4. Appuyez sur **Installer des applications inconnues**.
5. Selectionnez le gestionnaire de fichiers ou l'application que vous utilisez pour ouvrir l'APK, et activez l'option.

> La procedure exacte varie selon la marque et la version d'Android de votre telephone.

### Installer l'application

1. Ouvrez le fichier APK avec votre gestionnaire de fichiers.
2. Appuyez sur **Installer**.
3. Une fois l'installation terminee, appuyez sur **Ouvrir**.

---

## 3. Premier lancement

Au premier lancement, un ecran de bienvenue vous presente les principales fonctionnalites de l'application :

- **Transfert automatique** : chaque SMS et message RCS recu est renvoye vers le numero de votre choix.
- **Historique complet** : consultez tous les transferts avec leur statut.
- **100 % local** : aucune donnee n'est transmise a un serveur externe.

Un avertissement de securite vous rappelle que les SMS transitent en clair via le reseau de votre operateur. Les codes de verification (double authentification) seront egalement transferes : assurez-vous que le numero de destination est de confiance.

Appuyez sur **Commencer** pour acceder a l'application.

### Permissions a accepter

SMS Forwarder a besoin de trois permissions pour fonctionner. Android vous les demandera lors du premier lancement :

| Permission | A quoi elle sert |
|---|---|
| **Reception SMS** | Detecter les SMS entrants pour les transferer |
| **Envoi SMS** | Envoyer les SMS vers le numero de destination |
| **Notifications** | Afficher le service actif en arriere-plan |

Appuyez sur **Autoriser** pour chacune d'elles. Sans ces permissions, l'application ne peut pas fonctionner.

> Si vous avez refuse une permission par erreur, rendez-vous dans **Parametres du telephone > Applications > SMS Forwarder > Permissions** pour l'activer manuellement.

### Acces aux notifications (pour les messages RCS)

Pour que l'application puisse egalement capturer les messages RCS (Google Messages, Samsung Messages), elle doit acceder aux notifications de ces applications.

1. Dans l'application, appuyez sur **Reglages**.
2. Dans la section **Acces aux notifications**, appuyez sur **Configurer l'acces**.
3. Android ouvre les parametres systeme : activez **SMS Forwarder** dans la liste.
4. Revenez dans l'application. La mention **Acces active** confirme la configuration.

> Cette etape est facultative si vous n'utilisez pas de messages RCS.

---

## 4. Configurer le numero de destination

Avant d'activer le transfert, vous devez indiquer vers quel numero les SMS doivent etre renvoyes.

1. Depuis l'ecran d'accueil, appuyez sur **Reglages**.
2. Dans la section **Numero de destination**, saisissez le numero au format international : `+33 6 12 34 56 78`.
3. Appuyez sur **Enregistrer**. Un message confirme que le numero a ete enregistre.

### Tester la configuration

Pour verifier que tout fonctionne avant d'activer le transfert :

1. Dans **Reglages > Numero de destination**, appuyez sur **Envoyer un test**.
2. Un SMS `[SMS Forwarder] Ceci est un SMS de test.` est envoye au numero configure.
3. Verifiez que vous le recevez bien sur le telephone de destination.

> Le bouton **Envoyer un test** n'est disponible que si le numero saisi est valide.

---

## 5. Activer le transfert

Une fois le numero de destination configure, retournez sur l'ecran d'accueil.

1. Appuyez sur le **bouton bascule** (switch) au centre de la carte principale.
2. La carte passe en mode actif : l'icone et le texte affichent **Transfert actif**, et le numero de destination est indique en dessous.
3. Une notification permanente apparait dans la barre de votre telephone, indiquant que **SMS Forwarder est actif**. Cette notification est necessaire pour que le service continue de fonctionner meme quand l'application est fermee.

Pour desactiver le transfert, appuyez a nouveau sur le meme bouton bascule.

> Si le bouton bascule est grise, c'est que vous n'avez pas encore configure de numero de destination. Rendez-vous dans **Reglages** pour en saisir un.

---

## 6. Consulter l'historique

L'historique liste tous les SMS recus et le resultat de leur transfert.

1. Depuis l'ecran d'accueil, appuyez sur **Historique**.
2. Chaque ligne affiche l'expediteur, un extrait du message et le statut du transfert.

### Signification des statuts

| Statut | Couleur | Signification |
|---|---|---|
| **Envoye** | Vert | Le SMS a ete transfère avec succes |
| **Echoue** | Rouge | Le transfert a echoue (reseau indisponible, etc.) |
| **En attente** | Bleu | Le transfert est en cours ou en file d'attente |
| **Filtre** | Gris | Le SMS a ete bloque par une regle de filtrage |

### Rechercher dans l'historique

La barre de recherche en haut de l'ecran vous permet de filtrer les SMS par expediteur ou par contenu du message. Saisissez votre recherche : les resultats se mettent a jour en temps reel. Appuyez sur la croix pour effacer la recherche.

### Filtrer par statut

Sous la barre de recherche, des boutons de filtre rapide vous permettent d'afficher uniquement les SMS d'un statut particulier : **Tous**, **Envoyes**, **Echoues**, **En attente**, **Filtres**.

---

## 7. Retransmettre un SMS echoue

Si un transfert a echoue (reseau momentanement indisponible, par exemple), vous pouvez relancer l'envoi manuellement.

1. Dans l'**Historique**, appuyez sur la ligne du SMS echoue.
2. L'ecran de detail s'ouvre : vous voyez l'expediteur, le contenu complet du message, la date de reception, le nombre de tentatives precedentes et le message d'erreur si disponible.
3. Appuyez sur le bouton **Renvoyer ce SMS**.
4. Un message de confirmation apparait en bas de l'ecran une fois le renvoi effectue.

> L'application effectue aussi des relances automatiques (jusqu'a 3 tentatives, avec un delai croissant entre chaque essai). Le renvoi manuel est utile si vous souhaitez forcer une nouvelle tentative sans attendre.

---

## 8. Configurer les filtres

Les filtres vous permettent de controler quels SMS sont transferes. Par defaut, tous les SMS sont transferes sans restriction.

Acces : **Reglages > Gerer les regles de filtrage** ou directement via l'onglet **Filtres** dans la navigation.

### Choisir un mode de filtrage

En haut de l'ecran Filtres, trois modes sont disponibles :

| Mode | Comportement |
|---|---|
| **Aucun** | Tous les SMS sont transferes (aucun filtre) |
| **Liste blanche** | Seuls les SMS correspondant a vos regles sont transferes |
| **Liste noire** | Les SMS correspondant a vos regles sont bloques ; les autres sont transferes |

### Ajouter une regle

1. Selectionnez un mode (**Liste blanche** ou **Liste noire**).
2. Dans le champ **Numero ou mot-cle**, saisissez :
   - Un numero de telephone (exemple : `+33 6 12 34 56 78`) pour filtrer un expediteur precis.
   - Un mot ou une expression (exemple : `promo`, `code de verification`) pour filtrer par contenu.
3. Choisissez le type de la regle : **Liste blanche** ou **Liste noire**.
4. Appuyez sur **Ajouter**.

### Gerer les regles existantes

Dans la section **Regles actives**, chaque regle affiche le numero ou le mot-cle et son type. Vous pouvez :

- **Activer ou desactiver** une regle avec le bouton bascule sans la supprimer.
- **Supprimer** une regle avec le bouton corbeille.

---

## 9. Statistiques

L'ecran Statistiques vous donne une vue d'ensemble de l'activite de transfert.

Acces : depuis l'ecran d'accueil, appuyez sur **Statistiques**.

### Resume global

La section **Resume** affiche :

- **Total** : nombre total de SMS recus et traites.
- **Envoyes** : SMS transferes avec succes.
- **Echoues** : SMS dont le transfert a echoue.
- **Filtres** : SMS bloques par vos regles de filtrage.
- **En attente** : SMS dont le transfert n'est pas encore termine.
- **Taux de succes** : pourcentage de transferts reussis, accompagne d'une barre de progression.

### Graphique par jour

La section **Historique par jour** presente un graphique en barres de l'activite quotidienne. Selectionnez la periode souhaitee avec les boutons **7j**, **14j** ou **30j**. Les barres vertes representent les SMS envoyes, les barres rouges les echecs.

### Exporter l'historique en CSV

Pour exporter l'ensemble de votre historique :

1. Depuis l'ecran **Statistiques** ou **Historique**, appuyez sur **Exporter CSV**.
2. Android vous demande ou enregistrer le fichier. Choisissez un dossier sur votre telephone.
3. Le fichier est enregistre sous le nom `sms_export_[horodatage].csv`.

Le fichier CSV peut ensuite etre ouvert dans un tableur (Excel, Google Sheets, LibreOffice Calc).

---

## 10. Widget Android

Le widget vous permet d'activer ou de desactiver le transfert directement depuis votre ecran d'accueil, sans ouvrir l'application. Il affiche egalement le nombre de SMS transferes.

### Ajouter le widget

1. Appuyez longuement sur une zone vide de votre ecran d'accueil.
2. Appuyez sur **Widgets** (ou **Ajouter des widgets** selon votre telephone).
3. Recherchez **SMS Forwarder** dans la liste.
4. Appuyez longuement sur le widget et faites-le glisser vers l'emplacement souhaite sur votre ecran.

### Utiliser le widget

- Le widget affiche **ON** ou **OFF** selon l'etat actuel du transfert.
- Le compteur indique le nombre de SMS transferes.
- Appuyez sur le bouton bascule du widget pour activer ou desactiver le service instantanement.

---

## 11. Multi-SIM

Si votre telephone contient deux cartes SIM, vous pouvez choisir laquelle utiliser pour envoyer les SMS de transfert.

1. Allez dans **Reglages**.
2. La section **Multi-SIM** apparait automatiquement si deux cartes SIM sont detectees.
3. Selectionnez l'une des options :
   - **SIM par defaut** : utilise la carte SIM configuree par defaut dans votre telephone.
   - **SIM 1** : force l'utilisation de la premiere carte.
   - **SIM 2** : force l'utilisation de la deuxieme carte.

> Si la section Multi-SIM n'apparait pas, votre telephone ne contient qu'une seule carte SIM.

---

## 12. FAQ

Consultez le fichier [FAQ.md](FAQ.md) pour les questions frequentes detaillees.

**Questions rapides :**

- **Le transfert fonctionne-t-il si j'ai ferme l'application ?** Oui. Le service s'execute en arriere-plan grace a la notification permanente. Tant que cette notification est visible, le transfert est actif.
- **L'application redemarrerait-elle apres un reboot ?** Si le transfert etait actif avant l'arret du telephone, il redemarrera automatiquement au rallumage.
- **Comment eviter une boucle ?** L'application detecte automatiquement si le numero de destination correspond a l'un de vos numeros SIM locaux et bloque le transfert pour eviter une boucle infinie.
- **Les SMS transferes me coutent-ils de l'argent ?** Oui, chaque transfert consomme un SMS de votre forfait, au meme titre qu'un SMS envoye normalement.
