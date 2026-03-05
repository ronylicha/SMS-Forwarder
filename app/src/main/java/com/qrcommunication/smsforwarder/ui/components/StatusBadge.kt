package com.qrcommunication.smsforwarder.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, icon, label) = when (status) {
        SmsStatus.SENT.value -> StatusInfo(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Filled.CheckCircle,
            label = "Envoye"
        )
        SmsStatus.FAILED.value -> StatusInfo(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Filled.Error,
            label = "Echoue"
        )
        SmsStatus.PENDING.value -> StatusInfo(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Filled.Schedule,
            label = "En attente"
        )
        SmsStatus.FILTERED.value -> StatusInfo(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Filled.FilterList,
            label = "Filtre"
        )
        else -> StatusInfo(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Filled.Schedule,
            label = status
        )
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = modifier,
        shape = CircleShape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = null
    )
}

private data class StatusInfo(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val label: String
)
