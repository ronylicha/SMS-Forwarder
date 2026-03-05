# Politique de confidentialite — SMS Forwarder

*Derniere mise a jour : 4 mars 2026*

## Introduction

SMS Forwarder est une application Android developpee par **QrCommunication**. Cette politique de confidentialite decrit comment l'application traite vos donnees.

## Collecte de donnees

**SMS Forwarder ne collecte aucune donnee personnelle.**

L'application ne transmet aucune information a un serveur externe, a un service tiers, ou au developpeur. Il n'existe aucun systeme d'analytics, de tracking, de telemetrie ou de publicite integre dans l'application.

## Stockage des donnees

Toutes les donnees generees par l'application sont stockees **exclusivement sur votre appareil** :

| Donnee | Stockage | Description |
|--------|----------|-------------|
| Historique des SMS transferes | Base de donnees Room (SQLite locale) | Expediteur, contenu, statut de transfert, date |
| Regles de filtrage | Base de donnees Room (SQLite locale) | Numeros et mots-cles en liste blanche/noire |
| Preferences | SharedPreferences (locale) | Numero de destination, etat du service, preferences d'affichage |

Ces donnees ne quittent jamais votre appareil, sauf si vous utilisez explicitement la fonction d'export CSV.

## Transit des SMS

Les SMS transferes par l'application transitent **exclusivement via le reseau de votre operateur mobile**. L'application utilise les APIs standard d'Android pour envoyer les SMS. Aucun serveur intermediaire n'est implique dans le processus de transfert.

## Permissions utilisees

L'application requiert les permissions suivantes, chacune strictement necessaire a son fonctionnement :

| Permission | Justification |
|------------|---------------|
| `RECEIVE_SMS` | Intercepter les SMS entrants pour declencher le transfert automatique |
| `SEND_SMS` | Envoyer les SMS transferes vers le numero de destination configure |
| `READ_PHONE_STATE` | Lire le numero de la carte SIM locale pour la protection anti-boucle et le support multi-SIM |
| `RECEIVE_BOOT_COMPLETED` | Redemarrer automatiquement le service de transfert apres un redemarrage de l'appareil |
| `FOREGROUND_SERVICE` | Maintenir le service de transfert actif en arriere-plan de maniere fiable |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Declarer le type de service d'arriere-plan, requis depuis Android 14 |
| `POST_NOTIFICATIONS` | Afficher la notification persistante du service et les alertes de transfert, requis depuis Android 13 |

Aucune permission n'est utilisee a des fins de collecte de donnees ou de profilage.

## Securite

- **Protection anti-boucle** : l'application detecte automatiquement le numero de la carte SIM locale pour empecher les boucles infinies de transfert (un SMS transfere vers votre propre numero serait re-intercepte indefiniment)
- **Pas de secrets dans le code source** : la configuration de signature de l'application utilise des variables d'environnement
- **Aucune connexion reseau** : l'application n'etablit aucune connexion internet. Seul le reseau SMS de l'operateur est utilise

## Suppression des donnees

Vous pouvez supprimer toutes les donnees de l'application a tout moment :
- Depuis l'application : en vidant l'historique
- Depuis les parametres Android : Parametres > Applications > SMS Forwarder > Stockage > Effacer les donnees
- En desinstallant l'application : toutes les donnees sont automatiquement supprimees

## Modifications de cette politique

Toute modification de cette politique de confidentialite sera documentee dans le CHANGELOG de l'application et mise a jour dans ce document.

## Contact

Pour toute question relative a la confidentialite de vos donnees :

**QrCommunication**

---

*Cette application est concue dans le respect total de votre vie privee. Aucune donnee ne quitte votre appareil.*
