package com.qrcommunication.smsforwarder.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class CheckSeverity { OK, INFO, WARNING, ERROR }

@Composable
fun CheckSeverity.color(): Color = when (this) {
    CheckSeverity.OK -> MaterialTheme.colorScheme.primary
    CheckSeverity.INFO -> MaterialTheme.colorScheme.tertiary
    CheckSeverity.WARNING -> MaterialTheme.colorScheme.secondary
    CheckSeverity.ERROR -> MaterialTheme.colorScheme.error
}

fun CheckSeverity.icon(): ImageVector = when (this) {
    CheckSeverity.OK -> Icons.Filled.CheckCircle
    CheckSeverity.INFO -> Icons.Filled.Info
    CheckSeverity.WARNING -> Icons.Filled.Warning
    CheckSeverity.ERROR -> Icons.Filled.Error
}

/**
 * Ligne d'etat (diagnostics, audit permissions, dashboard checks).
 */
@Composable
fun StatusItem(
    severity: CheckSeverity,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = severity.icon(),
            contentDescription = null,
            tint = severity.color(),
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
