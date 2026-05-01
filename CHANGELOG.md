# Changelog

Toutes les modifications notables de ce projet sont documentees dans ce fichier.

Le format est base sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

## [1.3.1] - 2026-05-01

### Changed

- Premier release signe avec keystore de production pour Play Store. versionCode 5.

## [1.3.0] - 2026-05-01 (pre-release)

### Added

- **Onboarding multi-etapes** avec HorizontalPager 4 pages : Welcome / Permissions rationale / Configuration destination / Test SMS interactif. Chaque permission est expliquee individuellement.
- **Centre de notifications in-app** persiste en base : alertes erreur de regle, destination injoignable, quota, batterie. Mark-read individuel ou global, badge sur le dashboard.
- **Regles de transfert configurables** (CRUD complet) : critere expediteur (regex) + mot-cle, destination par regle (SMS ou webhook HTTP POST JSON), priorite, activation/desactivation. Test interactif d'une regle avec un SMS d'exemple.
- **Ecran Diagnostics** : audit des permissions, optimisation batterie, acces aux notifications, connectivite reseau. Chaque check propose un Intent vers les parametres systeme adapte.
- **Dashboard temps reel** : stats sur les dernieres 24h (envoyes/echoues), taux de succes global, regles actives, badge notifications non lues. Toutes les valeurs s'actualisent via Flow.
- **Filtre par destination et plage de dates** dans l'historique (DateRangePicker M3) + bouton Renvoyer inline sur les SMS echoues.
- **Politique de retry configurable** dans les reglages : tentatives max (slider 1-10), delai initial (chips 30s/1min/5min/15min), multiplicateur de backoff (x1.5/x2/x3).
- **Webhook HTTP POST JSON** comme type de destination : payload `{ sender, content, receivedAt, sourceLabel?, originalDestination? }` envoye sans dependance externe (HttpURLConnection).
- **Surveillance d'apps tierces** etendue (deja amorcee 1.2.x) : whitelist d'apps dont les notifications sont transferees comme des SMS (WhatsApp, Telegram, Allo, Ringover, Onoff...).

### Changed

- **MainScreen** evolue en Dashboard avec navigation vers 6 ecrans (Notifications, Regles, Historique, Diagnostics, Stats, Reglages) au lieu de 3.
- **Architecture Data** : nouvelle entity `ForwardingRule` distincte de `FilterRule` (SRP : filter pour le filtrage, ForwardingRule pour le routage). Migration Room v1->v2 non destructive.
- **Pipeline de transfert** : `SmsForwardService` consulte maintenant `MatchForwardingRuleUseCase` puis route via `DestinationDispatcher`. Si aucune regle ne match, fallback sur la destination globale (retro-compat 1.2.x).
- **Composants UI** factorises (DRY) : `SettingsCard`, `SectionHeader`, `EmptyState`, `StatTile`, `ConfigOption`, `StatusItem` partages entre tous les ecrans.

### Fixed

- Migration des APIs Android depreciees : `SmsManager.getDefault()` -> `getSystemService(SmsManager).createForSubscriptionId()`, `TelephonyManager.line1Number` -> `SubscriptionManager.getPhoneNumber(subId)` (API 33+ avec fallback < 33), `window.statusBarColor` -> `WindowCompat.setDecorFitsSystemWindows + insetsController`.
- 0 warning de compilation sur le code de production.

### Tests

- 185 tests unitaires (12 nouveaux fichiers de tests) avec Robolectric pour `DiagnosticsRunner` et serveur HTTP local pour `WebhookSender`. Couverture 100% sur les nouveaux modules.

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
