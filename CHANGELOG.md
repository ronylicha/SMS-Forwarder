# Changelog

Toutes les modifications notables de ce projet sont documentees dans ce fichier.

Le format est base sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

## [1.1.1] - 2026-03-05

### Fixed

- Correction de la dependance mockito-kotlin (5.8.0 n'existe pas, corrige en 5.4.0)
- Ajout de `<uses-feature android:name="android.hardware.telephony">` pour la compatibilite ChromeOS (lint error)
- Ajout de `unitTests.isReturnDefaultValues = true` pour corriger les RuntimeException dans les tests JVM
- Correction de la regex E.164 dans PhoneValidator pour rejeter les numeros trop courts (ex: `+33`)

## [1.0.0] - 2026-03-04

### Added

- Transfert automatique des SMS entrants vers un numero configure
- Ecran principal avec toggle ON/OFF et compteur de SMS transferes
- Ecran reglages avec validation du numero de destination au format E.164
- Historique des SMS transferes avec statuts (Envoye / Echoue / En attente / Filtre)
- Detail complet d'un SMS avec option de retransmission manuelle
- Retransmission manuelle depuis l'historique en cas d'echec
- Retry automatique (3 tentatives, backoff exponentiel)
- Envoi de SMS de test depuis les reglages
- Gestion des SMS multi-parties (messages > 160 caracteres)
- Compteur de SMS dans la notification permanente
- Systeme de filtres (liste blanche / liste noire) par numero ou mot-cle
- Export CSV de l'historique complet
- Statistiques avec resume global et graphique par jour
- Widget Android pour activer/desactiver le transfert rapidement
- Support multi-SIM avec selecteur de carte SIM
- Dark mode (suit automatiquement le theme systeme)
- Ecran d'onboarding au premier lancement
- Notification persistante via Foreground Service
- Redemarrage automatique du service apres reboot de l'appareil
- Protection anti-boucle (detection du numero SIM local)
- Architecture MVVM avec Jetpack Compose, Hilt et Room
- Suite de tests unitaires (utilitaires, use cases, ViewModels, services)
- Tests d'integration (flux SMS complet, sequence de boot)
