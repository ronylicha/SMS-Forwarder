# Changelog

Toutes les modifications notables sont documentées dans cette page.

Format basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Ce projet respecte le [Versionnage Sémantique](https://semver.org/lang/fr/).

---

## v1.4.0 — 21 juin 2026

### Internationalisation

- **Interface bilingue FR/EN** — toute l'interface utilisateur est traduite en français et anglais
- **Sélecteur de langue** dans les réglages, bascule instantanée sans redémarrage
- La langue suit par défaut la langue système au premier lancement

### Expérience utilisateur

- **Toasts de confirmation** sur chaque action importante (règle créée, destination enregistrée, test envoyé)
- Harmonisation des libellés et des chaînes de caractères

---

## v1.3.1 — 1 mai 2026

### Changed

- Premier release signé avec keystore de production pour Play Store. versionCode 5.

---

## v1.3.0 — 1 mai 2026

Première version majeure enrichie : dashboard, règles de transfert, webhook, notifications, diagnostics, surveillance d'apps tierces et retry configurable.

### Fonctionnalités

- **Onboarding multi-étapes** avec HorizontalPager 4 pages : Welcome / Permissions rationale / Configuration destination / Test SMS interactif. Chaque permission est expliquée individuellement.
- **Centre de notifications in-app** persisté en base : alertes erreur de règle, destination injoignable, quota, batterie. Mark-read individuel ou global, badge sur le dashboard.
- **Règles de transfert configurables** (CRUD complet) : critère expéditeur (regex) + mot-clé, destination par règle (SMS ou webhook HTTP POST JSON), priorité, activation/désactivation. Test interactif d'une règle avec un SMS d'exemple.
- **Écran Diagnostics** : audit des permissions, optimisation batterie, accès aux notifications, connectivité réseau. Chaque check propose un Intent vers les paramètres système adapté.
- **Dashboard temps réel** : stats sur les dernières 24h (envoyés/échoués), taux de succès global, règles actives, badge notifications non lues. Toutes les valeurs s'actualisent via Flow.
- **Filtre par destination et plage de dates** dans l'historique (DateRangePicker M3) + bouton Renvoyer inline sur les SMS échoués.
- **Politique de retry configurable** dans les réglages : tentatives max (slider 1-10), délai initial (chips 30s/1min/5min/15min), multiplicateur de backoff (x1.5/x2/x3).
- **Webhook HTTP POST JSON** comme type de destination : payload `{ sender, content, receivedAt, sourceLabel?, originalDestination? }` envoyé sans dépendance externe (HttpURLConnection).
- **Surveillance d'apps tierces** étendue : whitelist d'apps dont les notifications sont transférées comme des SMS (WhatsApp, Telegram, Allo, Ringover, Onoff...).

### Évolutions techniques

- **MainScreen** évolue en Dashboard avec navigation vers 6 écrans (Notifications, Règles, Historique, Diagnostics, Stats, Réglages) au lieu de 3.
- **Architecture Data** : nouvelle entité `ForwardingRule` distincte de `FilterRule` (SRP : filter pour le filtrage, ForwardingRule pour le routage). Migration Room v1→v2 non destructive.
- **Pipeline de transfert** : `SmsForwardService` consulte maintenant `MatchForwardingRuleUseCase` puis route via `DestinationDispatcher`. Si aucune règle ne match, fallback sur la destination globale (rétro-compat 1.2.x).
- **Composants UI** factorisés (DRY) : `SettingsCard`, `SectionHeader`, `EmptyState`, `StatTile`, `ConfigOption`, `StatusItem` partagés entre tous les écrans.

### Correctifs

- Migration des APIs Android dépréciées : `SmsManager.getDefault()` → `getSystemService(SmsManager).createForSubscriptionId()`, `TelephonyManager.line1Number` → `SubscriptionManager.getPhoneNumber(subId)` (API 33+ avec fallback < 33), `window.statusBarColor` → `WindowCompat.setDecorFitsSystemWindows + insetsController`.
- 0 warning de compilation sur le code de production.

### Tests

- 185 tests unitaires (12 nouveaux fichiers de tests) avec Robolectric pour `DiagnosticsRunner` et serveur HTTP local pour `WebhookSender`. Couverture 100% sur les nouveaux modules.

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
