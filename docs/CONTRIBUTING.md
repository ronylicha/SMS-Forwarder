# Guide de contribution — SMS Forwarder

## Table des matières

- [Prérequis](#prérequis)
- [Setup du projet](#setup-du-projet)
- [Structure du projet](#structure-du-projet)
- [Conventions de code](#conventions-de-code)
- [Workflow Git](#workflow-git)
- [Tests](#tests)
- [Ajouter un nouvel écran](#ajouter-un-nouvel-écran)
- [Ajouter un nouveau UseCase](#ajouter-un-nouveau-usecase)
- [Code review checklist](#code-review-checklist)

---

## Prérequis

| Outil | Version minimale | Notes |
|---|---|---|
| JDK | 17 | OpenJDK ou Adoptium Temurin |
| Android Studio | Ladybug (2024.2) ou plus récent | Plugin Kotlin 2.0 requis |
| Android SDK | API 36 (compileSdk) | Build Tools 34+ |
| Gradle | 8.11 (via wrapper) | Ne pas modifier la version manuellement |
| Git | 2.x | — |

Vérifier les versions installées :

```bash
java -version          # doit afficher 17.x
./gradlew --version    # doit afficher Gradle 8.11
```

---

## Setup du projet

### 1. Cloner le dépôt

```bash
git clone <url-du-depot>
cd app-pass-sms
```

### 2. Configurer `local.properties`

Ce fichier n'est pas versionné. Créez-le à la racine du projet :

```properties
# Chemin vers votre Android SDK
sdk.dir=/home/<utilisateur>/Android/Sdk
```

Android Studio le crée automatiquement. Si vous travaillez en ligne de commande, créez-le manuellement.

### 3. Build

```bash
# Build debug
./gradlew :app:assembleDebug

# Build release (nécessite les variables d'environnement de signature)
export KEYSTORE_FILE=/chemin/vers/keystore.jks
export KEYSTORE_PASSWORD=motdepasse
export KEY_ALIAS=alias
export KEY_PASSWORD=motdepasse
./gradlew :app:assembleRelease
```

### 4. Lancer les tests

```bash
# Tests unitaires
./gradlew :app:test

# Tests instrumentés (nécessite un émulateur ou appareil connecté)
./gradlew :app:connectedAndroidTest
```

### 5. Installer sur un appareil

```bash
./gradlew :app:installDebug
```

---

## Structure du projet

```
app-pass-sms/
├── app/
│   ├── build.gradle.kts            # Configuration du module applicatif
│   └── src/
│       ├── androidTest/            # Tests instrumentés (Room, flux d'intégration)
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/qrcommunication/smsforwarder/
│       │       ├── SmsForwarderApp.kt          # Application Hilt, canaux de notification
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   ├── AppDatabase.kt      # Déclaration Room
│       │       │   │   ├── dao/                # Interfaces DAO
│       │       │   │   └── entity/             # Entités + enums
│       │       │   ├── preferences/
│       │       │   │   └── PreferencesManager.kt
│       │       │   └── repository/             # Interfaces + implémentations
│       │       ├── di/
│       │       │   ├── DatabaseModule.kt       # Hilt : Room, DAOs
│       │       │   └── RepositoryModule.kt     # Hilt : liaisons repositories
│       │       ├── domain/
│       │       │   ├── usecase/                # 6 UseCases
│       │       │   └── validator/
│       │       │       └── FilterEngine.kt
│       │       ├── receiver/
│       │       │   ├── BootReceiver.kt
│       │       │   └── SmsReceiver.kt
│       │       ├── service/
│       │       │   ├── SmsForwardService.kt    # Foreground Service principal
│       │       │   ├── SmsContentObserver.kt
│       │       │   ├── NotificationInterceptorService.kt
│       │       │   ├── SmsSender.kt
│       │       │   ├── MessageDeduplicator.kt
│       │       │   ├── LoopProtection.kt
│       │       │   ├── SmsRetryManager.kt
│       │       │   └── NotificationHelper.kt
│       │       ├── ui/
│       │       │   ├── components/             # Composants Compose réutilisables
│       │       │   ├── detail/
│       │       │   ├── filter/
│       │       │   ├── history/
│       │       │   ├── main/
│       │       │   ├── navigation/
│       │       │   ├── onboarding/
│       │       │   ├── settings/
│       │       │   ├── stats/
│       │       │   ├── theme/
│       │       │   └── widget/
│       │       └── util/
│       │           ├── DateFormatter.kt
│       │           ├── PhoneValidator.kt
│       │           └── SmsFormatter.kt
│       └── test/                   # Tests unitaires (JVM)
├── build.gradle.kts                # Plugins racine
├── gradle/
│   └── libs.versions.toml          # Catalogue de versions (Version Catalog)
└── settings.gradle.kts
```

---

## Conventions de code

### Kotlin

**Nommage**

| Élément | Convention | Exemple |
|---|---|---|
| Classes | PascalCase | `SmsForwardService` |
| Fonctions | camelCase, verbe-nom | `handleForwardSms()`, `isLoopDetected()` |
| Variables | camelCase | `destinationNumber`, `retryCount` |
| Constantes | SCREAMING_SNAKE_CASE dans `companion object` | `MAX_RETRIES`, `ACTION_FORWARD_SMS` |
| Enums | PascalCase pour le type, SCREAMING_SNAKE_CASE pour les valeurs | `SmsStatus.FAILED` |
| Fichiers | PascalCase | `FilterEngine.kt` |

**Immutabilité**

```kotlin
// Correct : spread operator
val updated = record.copy(status = SmsStatus.SENT.value)

// Incorrect : mutation directe
record.status = SmsStatus.SENT.value // impossible car data class avec val
```

**Early return**

```kotlin
// Correct
if (destination.isBlank()) return ForwardResult.Skipped("No destination configured")
if (!preferencesManager.isForwardingEnabled) return ForwardResult.Skipped("Forwarding disabled")
// logique principale

// Incorrect : nesting
if (destination.isNotBlank()) {
    if (preferencesManager.isForwardingEnabled) {
        // logique enfouie
    }
}
```

**Coroutines**

- Toujours déclarer le `Dispatchers` explicitement dans les scopes de service (`Dispatchers.IO` pour I/O, `Dispatchers.Main` pour le UI).
- Utiliser `SupervisorJob()` pour les scopes de longue durée afin d'isoler les échecs.
- Préférer `flow { ... }` ou les `Flow` Room aux callbacks.

```kotlin
// Correct : scope supervisé dans un Service
private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

// Correct : opérations I/O parallèles
val (records, rules) = awaitAll(
    async { smsRepository.getAllRecords().first() },
    async { filterRepository.getActiveRules() }
)
```

**Gestion d'erreurs**

```kotlin
// Correct
return try {
    smsSender.sendSms(destination, message)
    smsRepository.updateStatus(recordId, SmsStatus.SENT)
    ForwardResult.Success(recordId)
} catch (e: Exception) {
    smsRepository.updateStatus(recordId, SmsStatus.FAILED, e.message)
    ForwardResult.Failed(recordId, e.message ?: "Unknown error")
}

// Incorrect : exception silencieuse
runCatching { smsSender.sendSms(destination, message) }
```

### Jetpack Compose

**State hoisting**

L'état doit remonter vers le ViewModel. Les composables `@Composable` sont sans état (stateless) dans la mesure du possible.

```kotlin
// Correct : état dans le ViewModel, composable reçoit des lambdas
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    PhoneNumberField(
        value = uiState.destinationNumber,
        onValueChange = viewModel::updateDestination
    )
}

// Incorrect : état local dans le composable pour des données persistées
@Composable
fun SettingsScreen() {
    var number by remember { mutableStateOf("") } // perdu à la rotation
}
```

**Conventions de nommage des composables**

- Les composables publics d'écran : `NomScreen` (ex. `MainScreen`, `HistingsScreen`).
- Les composables privés auxiliaires : préfixe `private`, nommés selon leur rôle (`HeroToggleCard`, `NavigationCard`, `StatsRow`).
- Les composables de composants réutilisables : nommés selon leur élément visuel (`StatusBadge`, `SmsListItem`).

**Paramètres**

- Toujours passer `Modifier` comme premier paramètre optionnel avec `Modifier` comme valeur par défaut.
- Les callbacks : nommés `onAction` (`onClick`, `onNavigateBack`, `onToggle`).

```kotlin
@Composable
fun StatusBadge(
    status: SmsStatus,
    modifier: Modifier = Modifier
) { ... }
```

### Organisation par feature

Chaque écran est dans son propre sous-package `ui/<feature>/` contenant le Screen et le ViewModel. Les composants partagés entre plusieurs écrans vont dans `ui/components/`.

---

## Workflow Git

### Branches

| Type | Nommage | Exemple |
|---|---|---|
| Fonctionnalité | `feature/<description>` | `feature/export-csv-filter` |
| Correction de bug | `fix/<description>` | `fix/loop-detection-e164` |
| Hotfix production | `hotfix/<description>` | `hotfix/crash-boot-receiver` |
| Branche principale | `main` | — |

### Commits conventionnels

Format : `type(scope): description courte`

| Type | Usage |
|---|---|
| `feat` | Nouvelle fonctionnalité |
| `fix` | Correction de bug |
| `refactor` | Refactoring sans changement de comportement |
| `test` | Ajout ou modification de tests |
| `docs` | Documentation uniquement |
| `chore` | Tâches de maintenance (dépendances, configuration) |
| `perf` | Amélioration de performance |

Exemples :
```
feat(filter): add keyword matching in message content
fix(loop): normalize 0033 prefix before comparison
test(forward): add test for missing destination case
refactor(dedup): extract hash generation to private method
```

### Pull Requests

- Une PR = une fonctionnalité ou un correctif.
- La description doit mentionner le problème résolu et la solution choisie.
- Les tests doivent passer avant de demander une review.
- Au moins une review approuvée est requise avant le merge.
- Pas de force push sur `main`.

---

## Tests

### Lancer les tests

```bash
# Tests unitaires (JVM) avec rapport de couverture
./gradlew :app:test

# Rapport HTML généré dans :
# app/build/reports/tests/testDebugUnitTest/index.html

# Tests instrumentés (émulateur ou appareil requis)
./gradlew :app:connectedAndroidTest
```

### Organisation des tests

```
src/test/         → Tests unitaires (JVM, pas d'Android runtime)
src/androidTest/  → Tests instrumentés (Room, intégration)
```

Les tests unitaires utilisent Mockito Kotlin pour les mocks. Les tests Room utilisent une base de données en mémoire (`Room.inMemoryDatabaseBuilder`).

### Pattern AAA

Chaque test suit le pattern Arrange-Act-Assert :

```kotlin
@Test
fun invoke_success_returnSuccess() = runTest {
    // Arrange
    whenever(preferencesManager.destinationNumber).thenReturn("+33699999999")
    whenever(preferencesManager.isForwardingEnabled).thenReturn(true)
    whenever(loopProtection.isLoopDetected(any(), any())).thenReturn(false)
    whenever(filterEngine.shouldForward(any(), any())).thenReturn(
        FilterResult(shouldForward = true, reason = "No filter active")
    )
    whenever(smsRepository.insertRecord(any())).thenReturn(1L)

    // Act
    val result = useCase(sender, content, timestamp)

    // Assert
    assertTrue(result is ForwardResult.Success)
    assertEquals(1L, (result as ForwardResult.Success).recordId)
}
```

### Nommage des tests

Format : `nomDeLaMethode_condition_comportementAttendu`

```kotlin
// Correct
fun shouldForward_whitelistMatch_returnsTrue()
fun isLoopDetected_normalizedMatch_returnsTrue()
fun invoke_forwardingDisabled_returnSkipped()

// Incorrect
fun test1()
fun works()
fun shouldWork()
```

### Cas à toujours couvrir

Pour chaque UseCase ou classe logique :

- Chemin nominal (happy path)
- Conditions de garde initiales (destination vide, transfert désactivé, boucle)
- Cas de filtrage (whitelist match, blacklist match, aucune règle)
- Échecs d'envoi (exception du SmsSender)
- Effets de bord (incrément du compteur, mise à jour du statut)

### Objectif de couverture

| Scope | Objectif |
|---|---|
| Global | >= 80% |
| Chemins critiques (ForwardSmsUseCase, FilterEngine, LoopProtection) | 100% |
| Nouveau code soumis en PR | >= 90% |

---

## Ajouter un nouvel écran

Checklist complète pour ajouter un écran `MonNouvelEcran` :

**1. Créer le package**

```
ui/monnouvelecran/
├── MonNouvelEcranScreen.kt
└── MonNouvelEcranViewModel.kt
```

**2. Créer le ViewModel**

```kotlin
data class MonNouvelEcranUiState(
    val isLoading: Boolean = true,
    // ... champs d'état
)

@HiltViewModel
class MonNouvelEcranViewModel @Inject constructor(
    private val monUseCase: MonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonNouvelEcranUiState())
    val uiState: StateFlow<MonNouvelEcranUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // ...
        }
    }
}
```

**3. Créer le composable d'écran**

```kotlin
@Composable
fun MonNouvelEcranScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonNouvelEcranViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon écran") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        // contenu
    }
}
```

**4. Ajouter la route dans `Screen.kt`**

```kotlin
sealed class Screen(val route: String) {
    // ...
    data object MonNouvelEcran : Screen("mon_nouvel_ecran")
}
```

**5. Ajouter la destination dans `AppNavigation.kt`**

```kotlin
composable(Screen.MonNouvelEcran.route) {
    MonNouvelEcranScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

**6. Ajouter le point d'accès depuis l'écran parent**

Passer une lambda `onNavigateToMonNouvelEcran` à l'écran parent et appeler `navController.navigate(Screen.MonNouvelEcran.route)`.

**7. Écrire les tests**

Créer `src/test/.../ui/MonNouvelEcranViewModelTest.kt` avec les cas nominaux et les cas d'erreur.

---

## Ajouter un nouveau UseCase

**1. Créer le fichier dans `domain/usecase/`**

```kotlin
class MonNouveauUseCase @Inject constructor(
    private val smsRepository: SmsRepository
    // ... autres dépendances
) {
    suspend operator fun invoke(param: String): ResultType {
        // logique métier pure
    }
}
```

Règles :
- Pas de dépendance sur des classes Android (Context, Intent, etc.) sauf si strictement nécessaire via `@ApplicationContext`.
- Les dépendances sont injectées par le constructeur, jamais instanciées directement.
- L'opérateur `invoke` est la méthode principale pour les UseCases à action unique.
- Retourner un `sealed class` pour les résultats multiples plutôt qu'une exception.

**2. Hilt injecte automatiquement le UseCase**

Aucun module Hilt à modifier : Hilt découvre les classes annotées `@Inject constructor` automatiquement si elles n'ont pas besoin d'une liaison d'interface.

**3. Injecter dans le ViewModel**

```kotlin
@HiltViewModel
class MonViewModel @Inject constructor(
    private val monNouveauUseCase: MonNouveauUseCase
) : ViewModel() { ... }
```

**4. Écrire les tests unitaires**

Créer `src/test/.../domain/usecase/MonNouveauUseCaseTest.kt`. Mocker toutes les dépendances avec Mockito Kotlin.

---

## Code review checklist

Avant de soumettre une PR, vérifier les points suivants :

**Logique**

- [ ] La fonctionnalité fonctionne selon les spécifications.
- [ ] Les conditions limites sont gérées (null, liste vide, valeur hors-plage).
- [ ] Pas de régression sur les fonctionnalités existantes.

**Code**

- [ ] Pas de `TODO` ou `FIXME` non justifié.
- [ ] Pas de `println` ou `Log.d` laissé en code de production critique.
- [ ] Pas de code mort ou commenté.
- [ ] Les noms sont descriptifs (pas de `x`, `temp`, `data2`).
- [ ] Les fonctions font moins de 50 lignes.
- [ ] Le nesting ne dépasse pas 3 niveaux (utiliser early return).

**Architecture**

- [ ] La couche Domain ne dépend pas de la couche Service ou UI.
- [ ] Les ViewModels ne contiennent pas de logique de navigation.
- [ ] Les composables ne font pas d'appels directs aux repositories.
- [ ] L'état UI est exposé via `StateFlow`, jamais via `LiveData`.

**Tests**

- [ ] Les nouvelles classes ont des tests unitaires.
- [ ] Les tests respectent le pattern AAA.
- [ ] Les tests couvrent les cas d'échec, pas seulement le happy path.
- [ ] La couverture globale ne régresse pas.

**Android**

- [ ] Les permissions déclarées dans le manifest correspondent aux usages dans le code.
- [ ] Les opérations d'I/O sont exécutées sur `Dispatchers.IO`.
- [ ] Les opérations UI sont exécutées sur `Dispatchers.Main`.
- [ ] Pas de fuite de contexte (Context stocké dans un singleton, etc.).
- [ ] Les ressources (curseurs, streams) sont fermées avec `use { }`.

**Sécurité**

- [ ] Pas de données sensibles dans les logs.
- [ ] Pas de credentials en dur dans le code.
- [ ] Les numéros de téléphone sont normalisés avant comparaison.
