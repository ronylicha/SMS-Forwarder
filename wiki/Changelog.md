# Changelog

Toutes les modifications notables sont documentées dans cette page.

Format basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Ce projet respecte le [Versionnage Sémantique](https://semver.org/lang/fr/).

---

## v1.0.0 — 4 mars 2026

Première version publique de SMS Forwarder.

### Transfert de messages

- Transfert automatique des SMS entrants vers un numéro configurable
- Triple capture : BroadcastReceiver (SMS classiques) + ContentObserver (`content://sms/inbox`, RCS) + NotificationListenerService (RCS via notifications)
- Déduplication intelligente — fenêtre de 5 secondes pour éviter les doublons entre sources
- Gestion des SMS multi-parties (messages supérieurs à 160 caractères)
- Retry automatique en cas d'échec : 3 tentatives avec backoff exponentiel (2s, 4s, 8s)
- Retransmission manuelle depuis l'écran de détail
- Protection anti-boucle par détection automatique du numéro SIM local

### Interface utilisateur

- Écran principal avec interrupteur ON/OFF et compteur de SMS transférés
- Écran de paramètres avec validation du numéro au format E.164
- Historique complet avec recherche et filtres par statut (Envoyé, Échoué, En attente, Filtré)
- Écran de détail avec possibilité de retransmission
- Statistiques avec résumé global et graphique d'activité par jour
- Widget Android pour activer ou désactiver le transfert depuis l'écran d'accueil
- Écran d'onboarding guidé au premier lancement
- Design Material You avec couleurs dynamiques (Android 12+)
- Dark mode automatique suivant le thème système

### Configuration

- Numéro de destination avec validation en temps réel
- SMS de test pour vérifier la configuration
- Système de filtres : aucun filtre / liste blanche / liste noire (par numéro ou mot-clé)
- Support multi-SIM avec sélecteur de carte SIM d'envoi
- Export CSV de l'historique complet

### Fiabilité et persistance

- Foreground Service avec notification persistante affichant le compteur de SMS
- Redémarrage automatique du service après reboot de l'appareil (`BOOT_COMPLETED`)
- Demande de permissions guidée au premier lancement

### Technique

- Architecture MVVM avec Jetpack Compose, Hilt (injection de dépendances) et Room (base de données locale)
- Suite de tests unitaires : 107 tests couvrant les utilitaires, use cases, ViewModels et services
- Tests d'intégration couvrant le flux SMS complet et la séquence de démarrage
- Aucune collecte de données, aucun analytics, stockage 100% local

---

[Retour à l'accueil](Home)
