<p align="center">
  <img src="docs/assets/logo.svg" width="128" height="128" alt="SMS Forwarder Logo">
</p>

<h1 align="center">SMS Forwarder</h1>

<p align="center">
  <a href="README.fr.md">🇫🇷 Français</a> | <a href="README.md">🇬🇧 English</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/API-26%2B-brightgreen" alt="API 26+">
  <img src="https://img.shields.io/badge/Kotlin-2.0-purple" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%20You-blue" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Android-8.0%2B-green" alt="Android">
  <img src="https://img.shields.io/badge/Release-v1.4.0-orange" alt="Release">
  <img src="https://img.shields.io/badge/License-AGPL%20v3-blue" alt="License">
</p>

**Application Android de transfert automatique de SMS, RCS et notifications d'apps** vers un numero de telephone, un webhook HTTP ou les deux. Concue pour etre fiable, configurable et respectueuse de la vie privee.

> Aucun compte requis. Aucune donnee envoyee a un serveur. 100% local. Bilingue FR/EN.

---

## Fonctionnalites

### Transfert
- **Transfert automatique** des SMS, RCS et notifications d'apps tierces (WhatsApp, Telegram, Allo, Ringover, Onoff...)
- **Triple capture** : BroadcastReceiver SMS + ContentObserver (RCS) + NotificationListener
- **Regles de transfert configurables** (CRUD) : critere expediteur (regex) + mot-cle, destination par regle (SMS ou webhook HTTP), priorite, activation/desactivation, test interactif
- **Webhook HTTP POST JSON** : envoyez chaque message vers n'importe quel endpoint `{ sender, content, receivedAt, sourceLabel?, originalDestination? }`
- **Deduplication intelligente** pour eviter les doublons entre sources
- **Retry configurable** : tentatives max (1-10), delai initial (30s/1min/5min/15min), backoff (x1.5/x2/x3)
- **Protection anti-boucle** (detection du numero SIM local)

### Interface
- **Dashboard temps reel** : stats 24h, taux de succes, regles actives, badge notifications
- **Design Material You** avec couleurs dynamiques (Android 12+)
- **Dark mode** automatique (suit le theme systeme)
- **Centre de notifications** in-app : alertes erreurs, destination injoignable, batterie
- **Ecran Diagnostics** : audit permissions, optimisation batterie, connectivite
- **Historique complet** avec recherche, filtres par statut/destination et plage de dates
- **Statistiques** avec resume et graphique par jour
- **Widget Android** pour activer/desactiver le transfert rapidement
- **Onboarding multi-etapes** guide au premier lancement
- **Internationalisation FR/EN** avec selecteur de langue dans les parametres

### Configuration
- **Filtrage avance** par liste blanche / liste noire (numero ou mot-cle)
- **Support multi-SIM** pour choisir la carte SIM d'envoi et de reception
- **Export CSV** de l'historique complet
- **SMS de test** pour valider la configuration

### Fiabilite
- **Foreground Service** avec notification persistante
- **Redemarrage automatique** apres reboot de l'appareil
- **Toasts de confirmation** sur toutes les actions (sauvegarde, changement de langue, etc.)

---

## Installation

### APK Direct

1. Telecharger l'APK depuis la [page Releases](https://github.com/ronylicha/SMS-Forwarder/releases)
2. Autoriser l'installation depuis des sources inconnues
3. Installer l'APK
4. Accorder les permissions demandees (SMS, Notifications)
5. Activer l'acces aux notifications pour capturer les RCS

### Google Play

Disponible sur le [Google Play Store](https://play.google.com/store/apps/details?id=com.qrcommunication.smsforwarder).

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 (Material You) |
| Architecture | MVVM + Clean Architecture + Ports & Adapters |
| Injection de dependances | Hilt (Dagger) |
| Base de donnees locale | Room v2 (SQLite) |
| Navigation | Navigation Compose |
| Asynchrone | Kotlin Coroutines + Flow |
| Internationalisation | values/ (EN default) + values-fr/ + values-en/ |
| SDK minimum | API 26 (Android 8.0 Oreo) |
| SDK cible | API 35 (Android 15) |
| Build | Gradle 8.11 (Kotlin DSL) + Version Catalog |

---

## Architecture

```
com.qrcommunication.smsforwarder/
├── data/                           # Couche donnees
│   ├── local/                      # Room DB (SmsRecord, FilterRule, ForwardingRule, AppNotification, DAOs)
│   ├── preferences/                # SharedPreferences wrapper (destination, langue, retry, etc.)
│   └── repository/                 # Repositories (SMS, Filter, ForwardingRule, AppNotification)
├── domain/                         # Couche metier
│   ├── model/                      # RetryPolicy, etc.
│   ├── usecase/                    # Forward, Retry, MatchRule, History, Stats, Export, Filters
│   └── validator/                  # FilterEngine (whitelist/blacklist)
├── service/                        # Services Android
│   ├── SmsForwardService.kt       # Foreground Service principal
│   ├── SmsReceiver.kt             # BroadcastReceiver SMS
│   ├── SmsContentObserver.kt      # ContentObserver pour RCS
│   ├── NotificationInterceptor.kt # NotificationListener pour RCS + apps tierces
│   ├── sender/                     # DestinationDispatcher, SmsSender, WebhookSender
│   ├── MessageDeduplicator.kt     # Anti-doublons
│   ├── SmsRetryManager.kt         # Retry avec backoff configurable
│   └── LoopProtection.kt          # Anti-boucle
├── ui/                             # Couche presentation (Compose)
│   ├── main/                       # Dashboard temps reel
│   ├── settings/                   # Configuration + selecteur de langue
│   ├── rules/                      # Regles de transfert (CRUD + test)
│   ├── history/                    # Historique avec filtres
│   ├── detail/                     # Detail SMS + retry
│   ├── notifications/              # Centre de notifications
│   ├── diagnostics/                # Audit systeme
│   ├── filter/                     # Gestion des filtres
│   ├── stats/                      # Statistiques
│   ├── onboarding/                 # Premier lancement
│   ├── appwhitelist/               # Surveillance d'apps tierces
│   ├── widget/                     # Widget Android
│   ├── components/                 # Composables partages
│   └── theme/                      # Theme Material You
├── util/                           # Utilitaires (PhoneValidator, DateFormatter, LocaleManager, DiagnosticsRunner)
├── di/                             # Modules Hilt
└── res/
    ├── values/strings.xml          # Anglais (default)
    ├── values-fr/strings.xml       # Francais
    └── values-en/strings.xml       # Anglais (explicite)
```

### Pipeline de transfert (v1.4.0)

```
SMS/RCS/Notif ──► SmsForwardService ──► MatchForwardingRuleUseCase
                                              │
                                    ┌─────────┴─────────┐
                                    │ regle match?      │
                                    └─────────┬─────────┘
                                     oui │         │ non
                                         │         └──► destination globale (fallback)
                                         ▼
                              DestinationDispatcher
                              ┌──────┴──────┐
                              │             │
                          SmsSender    WebhookSender
```

---

## Permissions requises

| Permission | Raison |
|------------|--------|
| `RECEIVE_SMS` | Intercepter les SMS entrants |
| `SEND_SMS` | Envoyer les SMS transferes |
| `READ_SMS` | Lire les RCS via ContentObserver |
| `READ_PHONE_STATE` | Detection SIM locale (anti-boucle) et multi-SIM |
| `RECEIVE_BOOT_COMPLETED` | Redemarrage automatique apres reboot |
| `FOREGROUND_SERVICE` | Service actif en arriere-plan |
| `POST_NOTIFICATIONS` | Notifications (Android 13+) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capture notifications RCS + apps tierces |

---

## Build

### Prerequis

- JDK 17+
- Android SDK avec API 36
- Gradle 8.11+ (via wrapper inclus)

### Compiler

```bash
# Debug
./gradlew assembleDebug

# Release (signe)
export KEYSTORE_FILE=path/to/keystore.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

### Tests

```bash
# Tests unitaires (185 tests)
./gradlew testDebugUnitTest

# Tests d'integration (sur appareil)
./gradlew connectedAndroidTest
```

---

## Internationalisation

L'application est entierement bilingue FR/EN :

| Composant | Fichier |
|-----------|---------|
| Anglais (default/fallback) | `res/values/strings.xml` (312 strings) |
| Francais | `res/values-fr/strings.xml` (312 strings) |
| Anglais (explicite) | `res/values-en/strings.xml` (312 strings) |
| Gestionnaire de locale | `util/LocaleManager.kt` |

**Comportement** : FR par defaut si le telephone est en francais, EN sinon. L'utilisateur peut forcer la langue dans **Parametres > Langue** (Système / Francais / English).

---

## Securite et confidentialite

- **Aucune collecte de donnees** — zero analytics, zero tracking
- **Stockage 100% local** — Room + SharedPreferences sur l'appareil
- **Transit operateur uniquement** — SMS envoyes via le reseau GSM de l'operateur
- **Webhook sous controle utilisateur** — les messages ne sont envoyes qu'a l'URL configuree
- **Protection anti-boucle** — detection automatique du numero SIM local
- **Aucun secret dans le code** — signing via variables d'environnement

Voir la [Politique de confidentialite](docs/PRIVACY.md) pour plus de details.

---

## Documentation

- [Guide utilisateur](docs/USER_GUIDE.md) — Installation et utilisation
- [Architecture](docs/ARCHITECTURE.md) — Detail technique de l'architecture
- [Base de donnees](docs/DATABASE.md) — Schema Room et migrations
- [FAQ](docs/FAQ.md) — Questions frequentes
- [Troubleshooting](docs/TROUBLESHOOTING.md) — Resolution de problemes
- [Confidentialite](docs/PRIVACY.md) — Politique de vie privee
- [Contribuer](docs/CONTRIBUTING.md) — Guide de contribution
- [Changelog](CHANGELOG.md) — Historique des versions
- [Site web](https://ronylicha.github.io/SMS-Forwarder/) — Landing page

---

## Licence

Ce projet est distribue sous la licence **GNU Affero General Public License v3.0 (AGPL-3.0)**.
Voir le fichier [LICENSE](LICENSE) pour les details.

Copyright (c) 2026 QrCommunication.

---

Developpe par **QrCommunication** — [qrcommunication.com](https://qrcommunication.com)
