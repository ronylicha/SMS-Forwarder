# Data Safety - Reponses Google Play

Ce document contient les reponses a fournir dans la section "Data safety" de la Google Play Console.

## Vue d'ensemble

| Question | Reponse |
|----------|---------|
| Votre application collecte-t-elle ou partage-t-elle des donnees utilisateur ? | Non |
| Votre application utilise-t-elle des donnees de localisation ? | Non |
| Votre application utilise-t-elle des identifiants publicitaires ? | Non |

## Collecte de donnees

**L'application ne collecte ni ne partage aucune donnee avec des tiers.**

### Types de donnees (toutes les cases a decocher) :

- [ ] Localisation
- [ ] Informations personnelles
- [ ] Informations financieres
- [ ] Sante et forme physique
- [ ] Messages (note : les SMS sont traites localement, jamais envoyes a un serveur)
- [ ] Photos et videos
- [ ] Fichiers audio
- [ ] Fichiers et documents
- [ ] Agenda
- [ ] Contacts
- [ ] Activite dans l'application
- [ ] Historique de navigation
- [ ] Identifiants de l'appareil ou autres
- [ ] Performance de l'application

## Securite

| Question | Reponse |
|----------|---------|
| Les donnees sont-elles chiffrees en transit ? | N/A (aucune donnee transmise a un serveur) |
| Les utilisateurs peuvent-ils demander la suppression de leurs donnees ? | Oui (desinstallation ou nettoyage depuis Parametres Android) |
| L'application est-elle conforme a la politique sur les familles ? | N/A (non destinee aux enfants) |

## Justification des permissions

A copier dans la declaration des permissions de la Play Console :

### RECEIVE_SMS
> Cette permission est necessaire pour la fonctionnalite principale de l'application : intercepter les SMS entrants afin de les transferer automatiquement vers un numero de telephone configure par l'utilisateur. Les SMS sont traites localement sur l'appareil et ne sont jamais envoyes a un serveur externe.

### SEND_SMS
> Cette permission est necessaire pour envoyer les SMS transferes vers le numero de destination configure par l'utilisateur. L'envoi se fait exclusivement via le reseau SMS de l'operateur mobile. Aucun serveur intermediaire n'est utilise.

### READ_SMS
> Cette permission est necessaire pour lire les messages RCS depuis la boite de reception via ContentObserver, car les RCS ne sont pas captures par le BroadcastReceiver SMS standard. Les messages sont traites localement et ne quittent jamais l'appareil.

### READ_PHONE_STATE
> Cette permission est necessaire pour deux fonctionnalites : (1) la protection anti-boucle, qui detecte le numero de la carte SIM locale pour empecher le transfert de SMS vers son propre numero, et (2) le support multi-SIM, qui permet a l'utilisateur de choisir la carte SIM d'envoi.

### FOREGROUND_SERVICE / FOREGROUND_SERVICE_SPECIAL_USE
> Le Foreground Service est necessaire pour maintenir le service de transfert SMS actif en arriere-plan de maniere fiable. Sans cette permission, Android pourrait arreter le service et les SMS ne seraient plus transferes. Une notification persistante informe l'utilisateur que le service est actif.

### POST_NOTIFICATIONS
> Cette permission (requise depuis Android 13) est necessaire pour afficher la notification persistante du Foreground Service et les alertes de transfert (succes/echec).

### RECEIVE_BOOT_COMPLETED
> Cette permission permet de redemarrer automatiquement le service de transfert SMS apres un redemarrage de l'appareil, sans intervention de l'utilisateur.

### BIND_NOTIFICATION_LISTENER_SERVICE
> Cette permission est necessaire pour capturer les messages RCS via les notifications systeme. C'est la troisieme methode de capture qui complete le BroadcastReceiver et le ContentObserver pour assurer une couverture maximale des messages.