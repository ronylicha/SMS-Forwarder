package com.qrcommunication.smsforwarder.ui.settings

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.qrcommunication.smsforwarder.R

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
                    text = stringResource(R.string.sim_selector_title),
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
                        text = stringResource(R.string.sim_permission_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (simInfos.isEmpty()) {
                Text(
                    text = stringResource(R.string.sim_none_detected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Automatic option
                SimOption(
                    label = stringResource(R.string.sim_auto),
                    subtitle = stringResource(R.string.sim_use_default),
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

@SuppressLint("MissingPermission")
private fun getAvailableSims(context: Context): List<SimInfo> {
    return try {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            ?: return emptyList()

        val activeSubscriptions: List<SubscriptionInfo> =
            subscriptionManager.activeSubscriptionInfoList ?: emptyList()

        activeSubscriptions.map { info ->
            val phone = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                runCatching { subscriptionManager.getPhoneNumber(info.subscriptionId) }
                    .getOrNull().orEmpty()
            } else {
                @Suppress("DEPRECATION")
                info.number.orEmpty()
            }
            SimInfo(
                slot = info.simSlotIndex,
                displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                carrierName = info.carrierName?.toString() ?: "",
                phoneNumber = phone,
            )
        }
    } catch (_: SecurityException) {
        emptyList()
    }
}
