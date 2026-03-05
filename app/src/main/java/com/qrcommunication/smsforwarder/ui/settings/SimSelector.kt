package com.qrcommunication.smsforwarder.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

data class SimInfo(
    val slot: Int,
    val displayName: String,
    val carrierName: String,
    val phoneNumber: String
)

@Composable
fun SimSelector(
    selectedSlot: Int,
    onSlotSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasPhonePermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    val simInfos = remember(hasPhonePermission) {
        if (hasPhonePermission) {
            getAvailableSims(context)
        } else {
            emptyList()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SimCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selection de la SIM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!hasPhonePermission) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "La permission READ_PHONE_STATE est requise pour detecter les cartes SIM. Accordez cette permission dans les parametres de l'application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (simInfos.isEmpty()) {
                Text(
                    text = "Aucune carte SIM detectee.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Automatic option
                SimOption(
                    label = "Automatique (par defaut)",
                    subtitle = "Utiliser la SIM par defaut du systeme",
                    selected = selectedSlot == -1,
                    onClick = { onSlotSelected(-1) }
                )

                // Each detected SIM
                simInfos.forEach { sim ->
                    SimOption(
                        label = sim.displayName,
                        subtitle = buildString {
                            append(sim.carrierName)
                            if (sim.phoneNumber.isNotBlank()) {
                                append(" - ")
                                append(sim.phoneNumber)
                            }
                        },
                        selected = selectedSlot == sim.slot,
                        onClick = { onSlotSelected(sim.slot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimOption(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getAvailableSims(context: Context): List<SimInfo> {
    return try {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            ?: return emptyList()

        val activeSubscriptions: List<SubscriptionInfo> =
            subscriptionManager.activeSubscriptionInfoList ?: emptyList()

        activeSubscriptions.map { info ->
            SimInfo(
                slot = info.simSlotIndex,
                displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                carrierName = info.carrierName?.toString() ?: "",
                phoneNumber = info.number ?: ""
            )
        }
    } catch (_: SecurityException) {
        emptyList()
    }
}
