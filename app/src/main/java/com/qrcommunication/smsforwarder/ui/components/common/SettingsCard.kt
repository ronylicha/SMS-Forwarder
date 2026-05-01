package com.qrcommunication.smsforwarder.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Card Material 3 standard pour sections de configuration et de contenu.
 *
 * Factorise le pattern recurrent : Card -> Column padding -> [Header] -> Content.
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    spacing: Int = 12,
    contentPadding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.dp),
        ) {
            if (title != null) {
                SectionHeader(title = title, icon = icon, iconTint = iconTint)
            }
            content()
        }
    }
}
