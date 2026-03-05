# Compiler depuis les sources

Ce guide est destiné aux développeurs qui souhaitent modifier l'application, contribuer au projet ou générer leur propre APK signé.

---

## Prérequis

| Outil | Version minimale | Remarque |
|---|---|---|
| JDK | 17 | OpenJDK ou Oracle JDK |
| Android SDK | API 36 (Android 15) | Installable via Android Studio |
| Gradle | 8.11 | Fourni par le wrapper, pas besoin d'installation manuelle |
| Android Studio | Hedgehog (2023.1) ou supérieur | Recommandé, mais optionnel |

Gradle est inclus dans le dépôt via le wrapper (`gradlew`). Il télécharge automatiquement la bonne version à la première exécution.

---

## Cloner le dépôt

```bash
git clone https://github.com/ronylicha/SMS-Forwarder.git
cd SMS-Forwarder
```

---

## Configurer local.properties

Le fichier `local.properties` indique à Gradle où se trouve votre Android SDK. Il est créé automatiquement par Android Studio. Si vous compilez en ligne de commande, créez-le manuellement :

```
# local.properties
sdk.dir=/home/votre-nom/Android/Sdk
```

Remplacez le chemin par l'emplacement réel de votre SDK :
- Linux/macOS : `~/Android/Sdk`
- Windows : `C:\Users\votre-nom\AppData\Local\Android\Sdk`

> Ce fichier est exclu du contrôle de version (`.gitignore`). Ne le committez jamais.

---

## Build debug

La version debug est signée avec une clé de développement générée automatiquement. Elle peut être installée directement sur un appareil ou un émulateur.

```bash
./gradlew assembleDebug
```

L'APK est généré dans :

```
app/build/outputs/apk/debug/app-debug.apk
```

Pour installer directement sur un appareil connecté en USB (débogage USB activé) :

```bash
./gradlew installDebug
```

---

## Build release

La version release doit être signée avec votre propre keystore. Les paramètres de signature sont lus depuis des **variables d'environnement** pour ne jamais stocker de secrets dans le code.

### Créer un keystore (si vous n'en avez pas)

```bash
keytool -genkeypair \
  -alias ma-cle \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore mon-keystore.jks
```

### Configurer les variables d'environnement

```bash
export KEYSTORE_FILE=/chemin/vers/mon-keystore.jks
export KEYSTORE_PASSWORD=mot-de-passe-keystore
export KEY_ALIAS=ma-cle
export KEY_PASSWORD=mot-de-passe-cle
```

### Lancer le build release

```bash
./gradlew assembleRelease
```

L'APK signé est généré dans :

```
app/build/outputs/apk/release/app-release.apk
```

---

## Lancer les tests

### Tests unitaires

107 tests couvrant les utilitaires, les use cases, les ViewModels et les services.

```bash
./gradlew testDebugUnitTest
```

Les rapports HTML sont générés dans :

```
app/build/reports/tests/testDebugUnitTest/index.html
```

### Tests d'intégration (sur appareil)

Ces tests nécessitent un appareil physique ou un émulateur connecté.

```bash
./gradlew connectedAndroidTest
```

---

## Structure du projet

```
app-pass-sms/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/qrcommunication/smsforwarder/
│   │   │   │   ├── data/           # Room, SharedPreferences, repositories
│   │   │   │   ├── domain/         # Use cases, FilterEngine
│   │   │   │   ├── service/        # Foreground service, receivers, déduplication
│   │   │   │   ├── ui/             # Jetpack Compose screens et ViewModels
│   │   │   │   ├── di/             # Modules Hilt
│   │   │   │   └── util/           # Formatage, validation
│   │   │   └── res/                # Ressources Android
│   │   └── test/                   # Tests unitaires
│   └── build.gradle.kts
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Version catalog Gradle
└── settings.gradle.kts
```

---

## Stack technique

| Composant | Technologie | Version |
|---|---|---|
| Langage | Kotlin | 2.0 |
| UI | Jetpack Compose + Material Design 3 | - |
| Architecture | MVVM (ViewModel + StateFlow) | - |
| Injection de dépendances | Hilt (Dagger) | - |
| Base de données | Room (SQLite) | - |
| Navigation | Navigation Compose | - |
| Asynchrone | Kotlin Coroutines + Flow | - |
| Build | Gradle 8.11 (Kotlin DSL) + Version Catalog | - |
| SDK minimum | API 26 (Android 8.0 Oreo) | - |
| SDK cible | API 35 (Android 15) | - |

---

## Contribuer

Les contributions sont bienvenues. Avant de soumettre une pull request :

1. Assurez-vous que tous les tests passent (`./gradlew testDebugUnitTest`)
2. Respectez l'architecture MVVM existante
3. Ajoutez des tests pour tout nouveau comportement
4. Documentez les changements dans le CHANGELOG

---

## Retour à l'accueil

[Home](Home)
