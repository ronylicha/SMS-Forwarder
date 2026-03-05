package com.qrcommunication.smsforwarder.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun PermissionHandler(
    onAllPermissionsGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showNotificationAccessDialog by remember { mutableStateOf(false) }
    var permissionRequestCompleted by remember { mutableStateOf(false) }
    var runtimePermissionsGranted by remember { mutableStateOf(false) }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.RECEIVE_SMS)
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.READ_SMS)
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val allGranted = remember(permissionRequestCompleted) {
        requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionRequestCompleted = true
        val allAccepted = results.values.all { it }
        if (allAccepted) {
            runtimePermissionsGranted = true
            // Verifier l'acces aux notifications pour RCS
            if (!isNotificationListenerEnabled(context)) {
                showNotificationAccessDialog = true
            } else {
                onAllPermissionsGranted()
            }
        } else {
            showRationaleDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (allGranted) {
            runtimePermissionsGranted = true
            if (!isNotificationListenerEnabled(context)) {
                showNotificationAccessDialog = true
            } else {
                onAllPermissionsGranted()
            }
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    if (showRationaleDialog) {
        PermissionRationaleDialog(
            onOpenSettings = {
                showRationaleDialog = false
                openAppSettings(context)
            },
            onDismiss = {
                showRationaleDialog = false
                onPermissionDenied()
            }
        )
    }

    if (showNotificationAccessDialog) {
        NotificationAccessDialog(
            onOpenSettings = {
                showNotificationAccessDialog = false
                openNotificationListenerSettings(context)
                onAllPermissionsGranted()
            },
            onSkip = {
                showNotificationAccessDialog = false
                onAllPermissionsGranted()
            }
        )
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver, "enabled_notification_listeners"
    )
    return enabledListeners?.contains(context.packageName) == true
}

private fun openNotificationListenerSettings(context: Context) {
    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

@Composable
private fun NotificationAccessDialog(
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkip,
        title = {
            Text(
                text = "Acces aux notifications",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pour capturer les messages RCS (Google Messages, Samsung Messages), SMS Forwarder a besoin d'acceder aux notifications.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sans cet acces, seuls les SMS classiques seront transferes. Les messages RCS ne seront pas detectes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onOpenSettings) {
                Text("Activer l'acces")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Plus tard")
            }
        }
    )
}

@Composable
private fun PermissionRationaleDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permissions requises",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SMS Forwarder a besoin des permissions suivantes pour fonctionner :",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "- Reception SMS : pour detecter les SMS entrants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "- Envoi SMS : pour transferer les SMS vers le numero configure",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "- Notifications : pour afficher le service en arriere-plan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Veuillez activer ces permissions dans les parametres de l'application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onOpenSettings) {
                Text("Ouvrir les parametres")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Plus tard")
            }
        }
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
