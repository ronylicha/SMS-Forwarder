# Guide de dépannage — SMS Forwarder

## Table des matières

- [Le service s'arrête tout seul](#le-service-sarrête-tout-seul)
- [Les RCS ne sont pas transférés](#les-rcs-ne-sont-pas-transférés)
- [Boucle de transfert infinie](#boucle-de-transfert-infinie)
- [Le SMS de test ne part pas](#le-sms-de-test-ne-part-pas)
- [L'application ne redémarre pas après un reboot](#lapplication-ne-redémarre-pas-après-un-reboot)
- [Messages dupliqués](#messages-dupliqués)
- [Le filtrage ne fonctionne pas comme attendu](#le-filtrage-ne-fonctionne-pas-comme-attendu)
- [Problèmes de build](#problèmes-de-build)

---

## Le service s'arrête tout seul

### Symptôme

L'application était active et le transfert fonctionnait, puis les messages ont cessé d'être transférés sans que l'utilisateur ait désactivé le service manuellement.

### Cause probable

Android gère aggressivement la mémoire et peut tuer les processus en arrière-plan, même les Foreground Services, selon les politiques OEM (constructeur du téléphone). Certains constructeurs comme Xiaomi, Huawei, Samsung ou OnePlus appliquent des restrictions supplémentaires au-delà de celles d'Android.

### Solutions

**1. Exclure l'application de l'optimisation batterie**

L'utilisateur doit aller dans : `Paramètres → Applications → SMS Forwarder → Batterie → Non restreinte` (le chemin exact varie selon le constructeur).

Sur les appareils Xiaomi (MIUI) : `Paramètres → Applications → SMS Forwarder → Économiseur de batterie → Aucune restriction` et activer `Démarrage automatique`.

Sur les appareils Huawei (EMUI) : `Paramètres → Gestion des applications → SMS Forwarder → Lancement des applications → Gérer manuellement` et activer les trois options (démarrage automatique, démarrage secondaire, activité en arrière-plan).

**2. Vérifier que la notification persistante est visible**

Si la notification du Foreground Service disparaît, le service s'est arrêté. La notification affiche "SMS Forwarder actif" avec le numéro de destination. Si elle n'est plus visible, relancer le service depuis l'interface principale.

**3. Comportement normal : `START_STICKY`**

`SmsForwardService` est configuré avec `START_STICKY`. Android est censé le relancer automatiquement après l'avoir tué. Ce redémarrage peut prendre quelques secondes à quelques minutes selon la charge système. Si le service ne redémarre pas du tout, le problème est une restriction OEM.

**4. Vérification dans le code**

Dans `SmsForwardService.onDestroy()`, le scope des coroutines est annulé et le `SmsContentObserver` est désenregistré. Si `onDestroy()` est appelé, le service a été arrêté proprement (action de l'utilisateur ou `ACTION_STOP_SERVICE`) ou tué par Android.

---

## Les RCS ne sont pas transférés

### Symptôme

Les SMS classiques sont bien transférés, mais les messages RCS (messages enrichis via Google Messages ou Samsung Messages) n'arrivent pas sur le numéro de destination.

### Cause

Les messages RCS ne déclenchent pas le broadcast `android.provider.Telephony.SMS_RECEIVED`. Le `SmsReceiver` ne les capture donc pas. L'application utilise deux mécanismes alternatifs :

1. **`SmsContentObserver`** : observe `content://sms/inbox`. Fonctionne si l'application de messagerie stocke les RCS dans le provider SMS standard.
2. **`NotificationInterceptorService`** : intercepte les notifications des applications de messagerie. Nécessite que l'accès aux notifications soit accordé.

### Solution : accorder l'accès aux notifications

L'accès aux notifications (NotificationListenerService) est une permission spéciale qui doit être accordée manuellement.

**Depuis l'application** : aller dans `Paramètres → Accès aux notifications` et appuyer sur le bouton pour ouvrir les paramètres système.

**Depuis Android** : `Paramètres → Applications → Accès spécial aux applications → Accès aux notifications → SMS Forwarder → Activer`.

Une fois l'accès accordé, l'écran Paramètres affiche `Accès notifications : Activé` en vert.

**Vérification dans le code** :

`SettingsViewModel.checkNotificationAccess()` vérifie la présence du package dans `Settings.Secure.getString(resolver, "enabled_notification_listeners")`. Cette vérification est relancée à chaque `onResume()` de l'écran Paramètres via un `LifecycleEventObserver`.

### Packages surveillés

`NotificationInterceptorService` surveille uniquement :
- `com.google.android.apps.messaging` (Google Messages)
- `com.samsung.android.messaging` (Samsung Messages)
- `com.android.mms` (AOSP Messages)

Si l'utilisateur utilise une autre application de messagerie RCS, elle ne sera pas couverte. Les messages passeront éventuellement par `SmsContentObserver` si l'application les écrit dans le provider SMS.

---

## Boucle de transfert infinie

### Symptôme

Le numéro de destination reçoit des messages transférés qui sont ensuite retransférés indéfiniment, ou l'application refuse de transférer en affichant "Loop detected" dans les logs.

### Cause

Une boucle se produit quand :
- Le numéro de destination est identique au numéro d'expéditeur d'un message transféré.
- Le numéro de destination correspond à l'un des numéros SIM de l'appareil source.

### Mécanisme de protection

`LoopProtection` effectue deux vérifications à chaque message :

1. **Comparaison directe** : `normalize(sender) == normalize(destination)`. Couvre le cas où le message transféré provient de la même SIM que la destination.

2. **Comparaison avec le numéro SIM local** : récupère `TelephonyManager.line1Number` et vérifie que la destination ne correspond pas à un numéro SIM de l'appareil. Cette vérification échoue silencieusement si la permission `READ_PHONE_STATE` est refusée.

La normalisation couvre les formats `+33XXXXXXXXX`, `0XXXXXXXXX`, `0033XXXXXXXXX` et les numéros avec espaces ou tirets.

### Solution

**Vérifier que le numéro de destination n'est pas un numéro SIM de l'appareil source.** Si l'appareil a deux SIMs et que la destination est le numéro de la SIM 2, les messages transférés via la SIM 1 seront reçus sur la SIM 2 et retransférés.

**Si la boucle persiste malgré la protection** : vérifier que `READ_PHONE_STATE` est accordée, car sans cette permission, `LoopProtection` ne peut lire le numéro SIM local et la vérification SIM est ignorée.

**Désactiver temporairement le transfert** via le toggle principal ou le widget si une boucle est en cours.

---

## Le SMS de test ne part pas

### Symptôme

Le bouton "Envoyer un test" dans l'écran Paramètres affiche un spinner puis le message "Echec de l'envoi : ..." apparaît en snackbar.

### Causes et solutions

**1. Permission `SEND_SMS` non accordée**

Android demande la permission `SEND_SMS` au premier lancement via `PermissionHandler`. Si elle a été refusée, aller dans `Paramètres Android → Applications → SMS Forwarder → Autorisations → SMS → Autoriser`.

**2. Numéro de destination invalide**

Le bouton de test est désactivé si `PhoneValidator.isValid()` retourne `false`. Les formats acceptés sont :
- E.164 : `+33612345678`
- Local français : `0612345678` ou `06 12 34 56 78`
- International avec double zéro : `0033612345678`

**3. Crédit opérateur insuffisant**

`SmsSender.sendSms()` appelle `SmsManager.sendTextMessage()` qui envoie un vrai SMS. Si la carte SIM n'a pas de crédit ou est en mode données seules, l'envoi échouera.

**4. Mode avion activé**

`SmsSender` n'effectue pas de vérification du mode avion. L'exception levée par `SmsManager` sera capturée et affichée.

**5. SIM sélectionnée incorrecte (appareils dual SIM)**

Si la section Multi-SIM est visible dans Paramètres, vérifier que le slot SIM sélectionné correspond à la SIM qui a les droits SMS et le crédit suffisant.

**Vérification dans le code** :

`SettingsViewModel.sendTestSms()` exécute l'envoi sur `Dispatchers.IO` et capture toute exception dans un `try/catch`. L'exception est affichée dans `testResult` via la snackbar.

---

## L'application ne redémarre pas après un reboot

### Symptôme

L'appareil a été redémarré. Le transfert SMS était actif avant le reboot, mais après le démarrage, les messages ne sont plus transférés et la notification du service n'est plus visible.

### Cause

`BootReceiver` est responsable du redémarrage automatique du service. Il écoute `BOOT_COMPLETED` et `QUICKBOOT_POWERON`. Si le service ne redémarre pas, l'une des causes suivantes est probable.

### Solutions

**1. Vérifier que le toggle est activé**

`BootReceiver` lit directement les `SharedPreferences` (`sms_forwarder_prefs`, clé `forwarding_enabled`). Si le toggle était désactivé avant le reboot, le service ne redémarrera pas. C'est le comportement attendu.

**2. Restrictions OEM sur le démarrage automatique**

Certains constructeurs (Xiaomi, Huawei, Oppo) bloquent le démarrage automatique des applications tiers. Activer le démarrage automatique dans les paramètres du constructeur :

- Xiaomi : `Paramètres → Applications → SMS Forwarder → Démarrage automatique → Activer`
- Huawei : `Paramètres → Applications → SMS Forwarder → Lancement des applications → Démarrage automatique`
- Samsung One UI : `Paramètres → Applications → SMS Forwarder → Batterie → Permettre l'activité en arrière-plan`

**3. Vérifier le délai de démarrage**

Sur certains appareils, `BOOT_COMPLETED` est envoyé plusieurs secondes après que le déverrouillage de l'écran soit nécessaire. Si l'appareil a un verrouillage par PIN/empreinte, attendre le déverrouillage avant de tester.

**4. Vérification dans le code**

`BootReceiver` utilise directement `context.getSharedPreferences()` sans Hilt (les BroadcastReceivers standard ne peuvent pas être `@AndroidEntryPoint` sans workaround). Si les clés de préférences sont mal synchronisées, il peut lire une valeur incorrecte.

Les constantes dans `BootReceiver` sont :
```kotlin
private const val PREF_NAME = "sms_forwarder_prefs"
private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
```

Ces valeurs doivent correspondre exactement à celles dans `PreferencesManager`.

---

## Messages dupliqués

### Symptôme

Le numéro de destination reçoit deux (ou plus) copies du même message SMS ou RCS.

### Cause

Les trois sources de capture (SmsReceiver, SmsContentObserver, NotificationInterceptorService) peuvent toutes détecter le même message simultanément. Sans mécanisme de déduplication, chaque source déclencherait un transfert séparé.

### Mécanisme de déduplication

`MessageDeduplicator` génère un hash pour chaque message basé sur :
- Le numéro d'expéditeur
- Les 100 premiers caractères du contenu
- Le timestamp arrondi à la borne des 5 secondes inférieure

Si le même hash est présenté deux fois dans la fenêtre de 60 secondes, le second appel retourne `false` et le message est ignoré.

### Solutions si des doublons persistent

**1. Timestamps très différents**

Si les deux sources rapportent des timestamps distants de plus de 5 secondes pour le même message (décalage possible avec les notifications RCS), les hashs seront différents. `TIMESTAMP_WINDOW_MS = 5000L` peut être ajusté.

**2. Sources actives non nécessaires**

Si tous les messages sont bien capturés par `SmsReceiver` seul (SMS classiques), désactiver l'accès aux notifications empêche `NotificationInterceptorService` de capturer les mêmes messages.

**3. Vérifier les sources actives**

Dans les logs (`adb logcat -s MessageDeduplicator`), les lignes `"Duplicate message detected"` confirment que la déduplication fonctionne. L'absence de ces lignes indique que les doublons proviennent d'une autre source.

```bash
adb logcat -s MessageDeduplicator SmsReceiver SmsContentObserver NotifInterceptor
```

---

## Le filtrage ne fonctionne pas comme attendu

### Symptôme A : Des messages qui devraient être bloqués (blacklist) passent quand même.

**Vérification 1** : Confirmer que le mode est bien `BLACKLIST` dans `Paramètres → Filtrage`. Si le mode est `NONE`, toutes les règles sont ignorées.

**Vérification 2** : Vérifier que la règle est active (toggle activé) dans l'écran de gestion des règles.

**Vérification 3** : Les numéros de téléphone dans les règles sont normalisés à l'évaluation. Une règle avec le pattern `0612345678` correspondra à un expéditeur `+33612345678`. Si l'expéditeur est un texte court (`SFR`, `AMAZON`), `PhoneValidator.isValid()` retournera `false` et le pattern sera traité comme un mot-clé, pas comme un numéro.

### Symptôme B : Des messages qui devraient passer (whitelist) sont bloqués.

**Vérification 1** : En mode `WHITELIST` avec au moins une règle active, tout message sans correspondance est bloqué. C'est le comportement attendu. Si aucune règle active n'existe, tous les messages passent (comportement de sécurité : `"No active rules"` retourne `shouldForward = true`).

**Vérification 2** : Les mots-clés sont recherchés dans le sender ET dans le content (logique OR). Un message dont le contenu contient le mot-clé passera même si l'expéditeur ne correspond pas.

**Logique `matchesRule()`** :

```kotlin
private fun matchesRule(rule: FilterRule, normalizedSender: String, content: String): Boolean {
    val pattern = rule.pattern.trim()
    if (PhoneValidator.isValid(pattern)) {
        // Numéro de téléphone : comparaison exacte après normalisation
        val normalizedPattern = PhoneValidator.normalize(pattern)
        return normalizedSender == normalizedPattern
    }
    // Mot-clé : recherche dans sender ET content (insensible à la casse)
    return normalizedSender.contains(pattern, ignoreCase = true) ||
           content.contains(pattern, ignoreCase = true)
}
```

---

## Problèmes de build

### Erreur : `SDK location not found`

Le fichier `local.properties` est absent ou le chemin SDK est incorrect.

```bash
# Créer local.properties à la racine du projet
echo "sdk.dir=/home/$USER/Android/Sdk" > local.properties
```

Sur macOS, le chemin par défaut est `/Users/$USER/Library/Android/sdk`.

### Erreur : `Kotlin version mismatch`

Le projet utilise Kotlin 2.0.21. Si Android Studio utilise une version différente, des conflits peuvent survenir.

Vérifier dans `gradle/libs.versions.toml` :
```toml
[versions]
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
```

La version KSP doit correspondre exactement à la version Kotlin (`2.0.21-X.Y.Z`).

### Erreur : `Hilt processor not found`

Hilt utilise KSP (Kotlin Symbol Processing). Vérifier que le plugin KSP est bien appliqué :

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
}
```

Ne pas confondre `ksp(...)` et `kapt(...)`. Le projet utilise exclusivement KSP.

### Erreur : `Room schema export directory not provided`

`AppDatabase` est configuré avec `exportSchema = false`. Si cette erreur apparaît, une configuration contradictoire existe. Vérifier qu'aucune option `room.schemaLocation` n'est définie dans `build.gradle.kts`.

### Build lent après une modification

Nettoyer le cache Gradle si les modifications ne sont pas prises en compte ou si le build échoue de façon inattendue :

```bash
./gradlew clean
./gradlew :app:assembleDebug
```

Si le problème persiste avec Android Studio, utiliser `File → Invalidate Caches and Restart`.

### Tests unitaires échouent avec `Method ... not mocked`

Les classes Android (comme `Log`, `SystemClock`) ne sont pas disponibles dans l'environnement JVM des tests unitaires. Ajouter dans `build.gradle.kts` :

```kotlin
android {
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
```

Cette option est déjà présente dans la configuration du projet. Si des erreurs persistent, vérifier que les classes Android sont bien mockées avec Mockito dans les tests concernés.
