# Installation

Ce guide couvre l'installation depuis un APK précompilé. Si vous souhaitez compiler vous-même l'application, consultez [Compiler depuis les sources](Building-From-Source).

---

## Prérequis

- Un appareil Android **8.0 (Oreo) ou supérieur** (API 26+)
- Une connexion pour télécharger l'APK
- Une carte SIM active avec la capacité d'envoyer des SMS

---

## Étape 1 — Télécharger l'APK

Rendez-vous sur la [page Releases](https://github.com/ronylicha/SMS-Forwarder/releases) du dépôt GitHub et téléchargez le fichier `SMS-Forwarder-v1.0.0.apk`.

---

## Étape 2 — Autoriser les sources inconnues

Android bloque par défaut l'installation d'APK provenant d'en dehors du Play Store. Il faut lever cette restriction pour le navigateur ou le gestionnaire de fichiers que vous utilisez.

**Android 8 et supérieur :**

1. Ouvrez **Paramètres**
2. Allez dans **Applications** (ou Gestion des applications)
3. Sélectionnez l'application depuis laquelle vous lancez l'installation (Chrome, Gestionnaire de fichiers, etc.)
4. Activez **Autoriser depuis cette source**

Cette autorisation n'est accordée qu'à cette application, pas au système entier.

---

## Étape 3 — Installer l'APK

Ouvrez le fichier `SMS-Forwarder-v1.0.0.apk` depuis votre gestionnaire de fichiers ou la barre de notifications (si vous l'avez téléchargé avec Chrome). Appuyez sur **Installer** et patientez quelques secondes.

---

## Étape 4 — Accorder les permissions

Au premier lancement, l'écran d'onboarding guide l'attribution des permissions nécessaires. Voici ce qui vous sera demandé et pourquoi :

| Permission | Raison |
|---|---|
| Recevoir les SMS | Intercepter les messages entrants en temps réel |
| Envoyer des SMS | Transférer les messages vers le numéro de destination |
| Lire les SMS | Détecter les messages RCS via le content provider |
| État du téléphone | Identifier le numéro SIM local (anti-boucle) et gérer le multi-SIM |
| Recevoir au démarrage | Relancer le service automatiquement après un reboot |
| Afficher les notifications | Afficher la notification persistante du service (Android 13+) |

Accordez chaque permission pour garantir le bon fonctionnement de l'application. Refuser une permission désactive la fonctionnalité correspondante.

---

## Étape 5 — Activer l'accès aux notifications (pour les RCS)

Les messages RCS (Google Messages, Samsung Messages) ne passent pas toujours par le système SMS standard. Pour les capturer, l'application a besoin d'un accès spécial aux notifications.

1. Dans l'application, allez dans **Paramètres**
2. Dans la section **Accès aux notifications**, appuyez sur **Ouvrir les paramètres**
3. Android vous amène dans **Paramètres système > Accès aux notifications**
4. Trouvez **SMS Forwarder** et activez le commutateur
5. Confirmez l'autorisation dans la boîte de dialogue

L'application affiche "Activé" dans la section correspondante une fois l'accès accordé.

> Si vous ne recevez que des SMS classiques, cette étape est optionnelle.

---

## Étape 6 — Premier lancement et configuration

1. Ouvrez **SMS Forwarder**
2. L'écran d'onboarding s'affiche au premier démarrage — suivez les étapes
3. Rendez-vous dans **Paramètres**
4. Saisissez le **numéro de destination** (voir [Configuration](Configuration) pour les formats acceptés)
5. Appuyez sur **Enregistrer**
6. Revenez sur l'écran principal et activez le **transfert** avec le bouton principal

L'application est prête. Envoyez un SMS de test depuis un autre téléphone pour vérifier que le transfert fonctionne.

---

## Vérifier que tout fonctionne

- La notification persistante "SMS Forwarder actif" doit apparaître dans la barre de notifications
- Dans **Paramètres**, utilisez le bouton **Envoyer un test** pour déclencher un SMS de vérification
- Consultez l'écran **Historique** pour voir le statut du message (Envoyé / Échoué)

---

## Prochaine étape

Configurez les filtres et le multi-SIM dans [Configuration](Configuration).
