# SMS Forwarder

![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%20You-blue)
![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Release](https://img.shields.io/badge/Release-v1.0.0-orange)
![License](https://img.shields.io/badge/License-AGPL%20v3-blue)

**Application Android de transfert automatique de SMS et RCS** vers un numero de telephone configure. Concue pour etre fiable, legere et respectueuse de la vie privee.

> Aucun compte requis. Aucune donnee envoyee a un serveur. 100% local.

---

## Fonctionnalites

### Transfert
- **Transfert automatique** des SMS et RCS entrants vers un numero configure
- **Triple capture** : BroadcastReceiver SMS + ContentObserver (RCS) + NotificationListener
- **Deduplication intelligente** pour eviter les doublons entre sources
- **Gestion SMS multi-parties** (messages > 160 caracteres)
- **Retry automatique** x3 avec backoff exponentiel (2s, 4s, 8s)
- **Retransmission manuelle** en cas d'echec depuis l'ecran de detail
- **Protection anti-boucle** (detection du numero SIM local)

### Interface
- **Design Material You** avec couleurs dynamiques (Android 12+)
- **Dark mode** automatique (suit le theme systeme)
- **Historique complet** avec recherche et filtres par statut
- **Statistiques** avec resume et graphique par jour
- **Widget Android** pour activer/desactiver le transfert rapidement
- **Onboarding** guide au premier lancement

### Configuration
- **Filtrage avance** par liste blanche / liste noire (numero ou mot-cle)
- **Support multi-SIM** pour choisir la carte SIM d'envoi
- **Export CSV** de l'historique complet
- **SMS de test** pour valider la configuration

### Fiabilite
- **Foreground Service** avec notification persistante
- **Redemarrage automatique** apres reboot de l'appareil
- **Demande de permissions** au premier lancement

---

## Installation

### APK Direct

1. Telecharger le fichier `SMS-Forwarder-v1.0.0.apk` depuis la [page Releases](https://github.com/ronylicha/SMS-Forwarder/releases)
2. Autoriser l'installation depuis des sources inconnues
3. Installer l'APK
4. Accorder les permissions demandees (SMS, Notifications)
5. Activer l'acces aux notifications pour capturer les RCS

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 (Material You) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Injection de dependances | Hilt (Dagger) |
| Base de donnees locale | Room (SQLite) |
| Navigation | Navigation Compose |
| Asynchrone | Kotlin Coroutines + Flow |
| SDK minimum | API 26 (Android 8.0 Oreo) |
| SDK cible | API 35 (Android 15) |
| Build | Gradle 8.11 (Kotlin DSL) + Version Catalog |

---

## Architecture

```
com.qrcommunication.smsforwarder/
├── data/                           # Couche donnees
│   ├── local/                      # Room DB (SmsRecord, FilterRule, DAOs)
│   ├── preferences/                # SharedPreferences wrapper
│   └── repository/                 # Repositories (SMS, Filter)
├── domain/                         # Couche metier
│   ├── usecase/                    # 6 UseCases (Forward, Retry, History, Stats, Export, Filters)
│   └── validator/                  # FilterEngine (whitelist/blacklist)
├── service/                        # Services Android
│   ├── SmsForwardService.kt       # Foreground Service principal
│   ├── SmsReceiver.kt             # BroadcastReceiver SMS
│   ├── SmsContentObserver.kt      # ContentObserver pour RCS
│   ├── NotificationInterceptor.kt # NotificationListener pour RCS
│   ├── MessageDeduplicator.kt     # Anti-doublons
│   ├── SmsSender.kt               # Envoi SMS (multi-SIM, multipart)
│   ├── SmsRetryManager.kt         # Retry avec backoff exponentiel
│   └── LoopProtection.kt          # Anti-boucle
├── ui/                             # Couche presentation (Compose)
│   ├── main/                       # Dashboard (toggle + compteur)
│   ├── history/                    # Historique avec recherche
│   ├── detail/                     # Detail SMS + retry
│   ├── settings/                   # Configuration
│   ├── filter/                     # Gestion des filtres
│   ├── stats/                      # Statistiques
│   ├── onboarding/                 # Premier lancement
│   ├── widget/                     # Widget Android
│   ├── components/                 # Composables partages
│   └── theme/                      # Theme Material You
├── di/                             # Modules Hilt
└── util/                           # Utilitaires (format SMS, validation tel, dates)
```

### Capture des messages (triple source)

```
SMS entrant ──► SmsReceiver (Broadcast) ──────────────────┐
                                                           │
RCS entrant ──► SmsContentObserver (content://sms/inbox) ──┼──► MessageDeduplicator ──► ForwardSmsUseCase ──► SmsSender
                                                           │
Notification ──► NotificationInterceptorService ───────────┘
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
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capture notifications RCS |

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

# Release
export KEYSTORE_FILE=path/to/keystore.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

### Tests

```bash
# Tests unitaires (107 tests)
./gradlew testDebugUnitTest

# Tests d'integration (sur appareil)
./gradlew connectedAndroidTest
```

---

## Securite et confidentialite

- **Aucune collecte de donnees** — zero analytics, zero tracking
- **Stockage 100% local** — Room + SharedPreferences sur l'appareil
- **Transit operateur uniquement** — SMS envoyes via le reseau GSM de l'operateur
- **Protection anti-boucle** — detection automatique du numero SIM local
- **Aucun secret dans le code** — signing via variables d'environnement

Voir la [Politique de confidentialite](docs/PRIVACY.md) pour plus de details.

---

## Licence

Ce projet est distribue sous la licence **GNU Affero General Public License v3.0 (AGPL-3.0)**.
Voir le fichier [LICENSE](LICENSE) pour les details.

Copyright (c) 2026 QrCommunication.

---

Developpe par **QrCommunication**
