package com.qrcommunication.smsforwarder.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.qrcommunication.smsforwarder.ui.components.common.CheckSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.qrcommunication.smsforwarder.R

data class DiagnosticCheck(
    val id: String,
    val title: String,
    val description: String,
    val severity: CheckSeverity,
    val fixIntent: Intent? = null,
    val fixLabel: String? = null,
)

/**
 * Audite l'environnement OS pour identifier les blocages potentiels du transfert :
 * permissions, notifications, battery optimization, reseau.
 *
 * Pas de side-effect, juste des checks read-only. Les fixes sont des Intents
 * exposes via DiagnosticCheck.fixIntent que l'UI peut lancer.
 */
@Singleton
class DiagnosticsRunner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun runAll(): List<DiagnosticCheck> = buildList {
        addAll(permissionChecks())
        add(notificationListenerCheck())
        add(batteryOptimizationCheck())
        add(networkCheck())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(postNotificationsCheck())
        }
    }

    private fun permissionChecks(): List<DiagnosticCheck> {
        val perms = mapOf(
            Manifest.permission.RECEIVE_SMS to "Reception SMS",
            Manifest.permission.SEND_SMS to "Envoi SMS",
            Manifest.permission.READ_SMS to "Lecture SMS",
            Manifest.permission.READ_PHONE_STATE to context.getString(R.string.diag_phone_state),
        )
        return perms.map { (perm, label) ->
            val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            DiagnosticCheck(
                id = "perm_$perm",
                title = label,
                description = if (granted) "Permission accordee" else "Permission manquante",
                severity = if (granted) CheckSeverity.OK else CheckSeverity.ERROR,
                fixIntent = if (granted) null else appSettingsIntent(),
                fixLabel = if (granted) null else "Ouvrir parametres",
            )
        }
    }

    private fun postNotificationsCheck(): DiagnosticCheck {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        return DiagnosticCheck(
            id = "post_notifications",
            title = context.getString(R.string.diagnostics_notification_display),
            description = if (granted) "Autorise" else "Refuse - le service foreground ne peut pas afficher de status",
            severity = if (granted) CheckSeverity.OK else CheckSeverity.WARNING,
            fixIntent = if (granted) null else appSettingsIntent(),
            fixLabel = if (granted) null else "Activer",
        )
    }

    private fun notificationListenerCheck(): DiagnosticCheck {
        val enabled = isNotificationListenerEnabled()
        return DiagnosticCheck(
            id = "notification_listener",
            title = context.getString(R.string.diagnostics_notification_access_rcs),
            description = if (enabled) {
                "Active - les RCS et apps tierces peuvent etre interceptes"
            } else {
                "Desactive - les RCS et apps tierces ne seront pas captures"
            },
            severity = if (enabled) CheckSeverity.OK else CheckSeverity.WARNING,
            fixIntent = if (enabled) null else Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
            fixLabel = if (enabled) null else "Activer",
        )
    }

    private fun batteryOptimizationCheck(): DiagnosticCheck {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val ignoring = pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        @Suppress("BatteryLife")
        val fixIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
        return DiagnosticCheck(
            id = "battery_optimization",
            title = context.getString(R.string.diag_battery_optimization),
            description = if (ignoring) {
                "Exception accordee - le service ne sera pas suspendu"
            } else {
                "Service susceptible d'etre suspendu en arriere-plan par Android"
            },
            severity = if (ignoring) CheckSeverity.OK else CheckSeverity.WARNING,
            fixIntent = if (ignoring) null else fixIntent,
            fixLabel = if (ignoring) null else "Demander exception",
        )
    }

    private fun networkCheck(): DiagnosticCheck {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val capabilities = network?.let { cm.getNetworkCapabilities(it) }
        val online = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return DiagnosticCheck(
            id = "network",
            title = context.getString(R.string.diag_internet_connectivity),
            description = if (online) {
                "En ligne (necessaire pour les destinations webhook)"
            } else {
                "Hors ligne - les webhooks vont echouer (les SMS continuent de fonctionner)"
            },
            severity = if (online) CheckSeverity.OK else CheckSeverity.INFO,
        )
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners",
        )
        return enabledListeners?.contains(context.packageName) == true
    }

    private fun appSettingsIntent(): Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", context.packageName, null)
    }
}
