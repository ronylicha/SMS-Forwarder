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

**Android app for automatic forwarding of SMS, RCS and app notifications** to a phone number, an HTTP webhook, or both. Designed to be reliable, configurable and privacy-respecting.

> No account required. No data sent to a server. 100% local. Bilingual FR/EN.

---

## Features

### Forwarding
- **Automatic forwarding** of SMS, RCS and third-party app notifications (WhatsApp, Telegram, Allo, Ringover, Onoff...)
- **Triple capture**: SMS BroadcastReceiver + ContentObserver (RCS) + NotificationListener
- **Configurable forwarding rules** (CRUD): sender criteria (regex) + keyword, per-rule destination (SMS or HTTP webhook), priority, enable/disable, interactive test
- **HTTP POST JSON webhook**: send each message to any endpoint `{ sender, content, receivedAt, sourceLabel?, originalDestination? }`
- **Smart deduplication** to avoid duplicates between sources
- **Configurable retry**: max attempts (1-10), initial delay (30s/1min/5min/15min), backoff (x1.5/x2/x3)
- **Anti-loop protection** (local SIM number detection)

### Interface
- **Real-time dashboard**: 24h stats, success rate, active rules, notification badge
- **Material You design** with dynamic colors (Android 12+)
- **Automatic dark mode** (follows system theme)
- **In-app notification center**: error alerts, unreachable destination, battery warnings
- **Diagnostics screen**: permission audit, battery optimization, connectivity
- **Full history** with search, filters by status/destination and date range
- **Statistics** with summary and daily chart
- **Android widget** to quickly toggle forwarding
- **Multi-step onboarding** at first launch
- **FR/EN internationalization** with language selector in settings

### Configuration
- **Advanced filtering** by whitelist / blacklist (number or keyword)
- **Multi-SIM support** for choosing sending and receiving SIM card
- **CSV export** of full history
- **Test SMS** to validate configuration

### Reliability
- **Foreground Service** with persistent notification
- **Automatic restart** after device reboot
- **Toast confirmations** on all actions (save, language change, etc.)

---

## Installation

### Direct APK

1. Download the APK from the [Releases page](https://github.com/ronylicha/SMS-Forwarder/releases)
2. Allow installation from unknown sources
3. Install the APK
4. Grant the requested permissions (SMS, Notifications)
5. Enable notification access to capture RCS

### Google Play

Available on the [Google Play Store](https://play.google.com/store/apps/details?id=com.qrcommunication.smsforwarder).

---

## Tech Stack

| Component | Technology |
|-----------|-------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 (Material You) |
| Architecture | MVVM + Clean Architecture + Ports & Adapters |
| Dependency Injection | Hilt (Dagger) |
| Local Database | Room v2 (SQLite) |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| i18n | values/ (EN default) + values-fr/ + values-en/ |
| Min SDK | API 26 (Android 8.0 Oreo) |
| Target SDK | API 35 (Android 15) |
| Build | Gradle 8.11 (Kotlin DSL) + Version Catalog |

---

## Architecture

```
com.qrcommunication.smsforwarder/
├── data/                           # Data layer
│   ├── local/                      # Room DB (SmsRecord, FilterRule, ForwardingRule, AppNotification, DAOs)
│   ├── preferences/                # SharedPreferences wrapper (destination, language, retry, etc.)
│   └── repository/                 # Repositories (SMS, Filter, ForwardingRule, AppNotification)
├── domain/                         # Domain layer
│   ├── model/                      # RetryPolicy, etc.
│   ├── usecase/                    # Forward, Retry, MatchRule, History, Stats, Export, Filters
│   └── validator/                  # FilterEngine (whitelist/blacklist)
├── service/                        # Android Services
│   ├── SmsForwardService.kt       # Main Foreground Service
│   ├── SmsReceiver.kt             # SMS BroadcastReceiver
│   ├── SmsContentObserver.kt      # ContentObserver for RCS
│   ├── NotificationInterceptor.kt # NotificationListener for RCS + third-party apps
│   ├── sender/                     # DestinationDispatcher, SmsSender, WebhookSender
│   ├── MessageDeduplicator.kt     # Anti-duplicates
│   ├── SmsRetryManager.kt         # Retry with configurable backoff
│   └── LoopProtection.kt          # Anti-loop
├── ui/                             # Presentation layer (Compose)
│   ├── main/                       # Real-time dashboard
│   ├── settings/                   # Configuration + language selector
│   ├── rules/                      # Forwarding rules (CRUD + test)
│   ├── history/                    # History with filters
│   ├── detail/                     # SMS detail + retry
│   ├── notifications/              # Notification center
│   ├── diagnostics/                # System audit
│   ├── filter/                     # Filter management
│   ├── stats/                      # Statistics
│   ├── onboarding/                 # First launch
│   ├── appwhitelist/               # Third-party app monitoring
│   ├── widget/                     # Android widget
│   ├── components/                 # Shared composables
│   └── theme/                      # Material You theme
├── util/                           # Utilities (PhoneValidator, DateFormatter, LocaleManager, DiagnosticsRunner)
├── di/                             # Hilt modules
└── res/
    ├── values/strings.xml          # English (default)
    ├── values-fr/strings.xml       # French
    └── values-en/strings.xml       # English (explicit)
```

### Forwarding pipeline (v1.4.0)

```
SMS/RCS/Notif ──► SmsForwardService ──► MatchForwardingRuleUseCase
                                              │
                                    ┌─────────┴─────────┐
                                    │ rule match?       │
                                    └─────────┬─────────┘
                                     yes │         │ no
                                         │         └──► global destination (fallback)
                                         ▼
                              DestinationDispatcher
                              ┌──────┴──────┐
                              │             │
                          SmsSender    WebhookSender
```

---

## Required Permissions

| Permission | Reason |
|------------|--------|
| `RECEIVE_SMS` | Intercept incoming SMS |
| `SEND_SMS` | Send forwarded SMS |
| `READ_SMS` | Read RCS via ContentObserver |
| `READ_PHONE_STATE` | Local SIM detection (anti-loop) and multi-SIM |
| `RECEIVE_BOOT_COMPLETED` | Automatic restart after reboot |
| `FOREGROUND_SERVICE` | Active background service |
| `POST_NOTIFICATIONS` | Notifications (Android 13+) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capture RCS + third-party app notifications |

---

## Build

### Prerequisites

- JDK 17+
- Android SDK with API 36
- Gradle 8.11+ (via included wrapper)

### Compile

```bash
# Debug
./gradlew assembleDebug

# Release (signed)
export KEYSTORE_FILE=path/to/keystore.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

### Tests

```bash
# Unit tests (185 tests)
./gradlew testDebugUnitTest

# Integration tests (on device)
./gradlew connectedAndroidTest
```

---

## Internationalization

The app is fully bilingual FR/EN:

| Component | File |
|-----------|------|
| English (default/fallback) | `res/values/strings.xml` (312 strings) |
| French | `res/values-fr/strings.xml` (312 strings) |
| English (explicit) | `res/values-en/strings.xml` (312 strings) |
| Locale manager | `util/LocaleManager.kt` |

**Behavior**: FR by default if the phone is in French, EN otherwise. The user can force the language in **Settings > Language** (System / French / English).

---

## Security & Privacy

- **No data collection** — zero analytics, zero tracking
- **100% local storage** — Room + SharedPreferences on device
- **Carrier transit only** — SMS sent via the carrier's GSM network
- **User-controlled webhook** — messages are only sent to the URL you configure
- **Anti-loop protection** — automatic local SIM number detection
- **No secrets in code** — signing via environment variables

See the [Privacy Policy](docs/PRIVACY.md) for more details.

---

## Documentation

- [User Guide](docs/USER_GUIDE.md) — Installation and usage
- [Architecture](docs/ARCHITECTURE.md) — Technical architecture details
- [Database](docs/DATABASE.md) — Room schema and migrations
- [FAQ](docs/FAQ.md) — Frequently asked questions
- [Troubleshooting](docs/TROUBLESHOOTING.md) — Problem solving
- [Privacy](docs/PRIVACY.md) — Privacy policy
- [Contributing](docs/CONTRIBUTING.md) — Contribution guide
- [Changelog](CHANGELOG.md) — Version history
- [Website](https://ronylicha.github.io/SMS-Forwarder/) — Landing page

---

## License

This project is distributed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.
See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 QrCommunication.

---

Developed by **QrCommunication** — [qrcommunication.com](https://qrcommunication.com)
