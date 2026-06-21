package com.qrcommunication.smsforwarder.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.ui.components.common.StatTile
import com.qrcommunication.smsforwarder.util.PhoneValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    onNavigateToRules: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) { viewModel.refreshState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("SMS Forwarder", fontWeight = FontWeight.Bold) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            HeroToggleCard(
                isEnabled = uiState.isForwardingEnabled,
                isDestinationConfigured = uiState.isDestinationConfigured,
                destinationNumber = uiState.destinationNumber,
                activeRulesCount = uiState.activeRulesCount,
                onToggle = viewModel::toggleForwarding,
            )

            AnimatedVisibility(
                visible = !uiState.isDestinationConfigured && uiState.activeRulesCount == 0,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.95f),
            ) {
                MissingDestinationBanner()
            }

            // Stats 24h
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    value = "${uiState.sentLast24h}",
                    label = stringResource(R.string.main_sent_24h),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${uiState.failedLast24h}",
                    label = stringResource(R.string.main_failed_24h),
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }

            // Cumul total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    value = "${uiState.totalForwarded}",
                    label = stringResource(R.string.main_total_forwarded),
                    modifier = Modifier.weight(1f),
                )
                val total = uiState.totalForwarded + uiState.totalFailed
                val rate = if (total > 0) uiState.totalForwarded.toFloat() / total else 0f
                StatTile(
                    value = "%.0f%%".format(rate * 100),
                    label = stringResource(R.string.stats_success_rate),
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    progress = rate,
                    progressColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }

            // Navigation cards (factorisees via DRY NavigationCard)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NavigationCard(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.main_notifications),
                    subtitle = if (uiState.unreadNotifications > 0) {
                        "${uiState.unreadNotifications} non lues"
                    } else {
                        "Aucun nouvel evenement"
                    },
                    badgeCount = uiState.unreadNotifications,
                    onClick = onNavigateToNotifications,
                )
                NavigationCard(
                    icon = Icons.AutoMirrored.Filled.Rule,
                    title = stringResource(R.string.main_rules_transfer),
                    subtitle = stringResource(R.string.main_active_rules_count, uiState.activeRulesCount),
                    onClick = onNavigateToRules,
                )
                NavigationCard(
                    icon = Icons.Filled.History,
                    title = stringResource(R.string.history_title),
                    subtitle = stringResource(R.string.main_view_all_transfers),
                    onClick = onNavigateToHistory,
                )
                NavigationCard(
                    icon = Icons.Filled.HealthAndSafety,
                    title = stringResource(R.string.main_diagnostics),
                    subtitle = stringResource(R.string.main_audit_permissions),
                    onClick = onNavigateToDiagnostics,
                )
                NavigationCard(
                    icon = Icons.Filled.BarChart,
                    title = stringResource(R.string.main_statistics),
                    subtitle = stringResource(R.string.main_view_trends),
                    onClick = onNavigateToStats,
                )
                NavigationCard(
                    icon = Icons.Filled.Settings,
                    title = stringResource(R.string.settings_title),
                    subtitle = stringResource(R.string.main_configure_forwarding),
                    onClick = onNavigateToSettings,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MissingDestinationBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Text(
            text = stringResource(R.string.main_no_destination_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun HeroToggleCard(
    isEnabled: Boolean,
    isDestinationConfigured: Boolean,
    destinationNumber: String,
    activeRulesCount: Int,
    onToggle: () -> Unit,
) {
    val canToggle = isDestinationConfigured || activeRulesCount > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedContent(
                targetState = isEnabled,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
                },
                label = "icon_transition",
            ) { enabled ->
                Icon(
                    imageVector = if (enabled) Icons.AutoMirrored.Filled.PhoneForwarded
                    else Icons.Filled.PhoneDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                enabled = canToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Text(
                text = if (isEnabled) "Transfert actif" else "Transfert inactif",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    isDestinationConfigured -> PhoneValidator.formatDisplay(destinationNumber)
                    activeRulesCount > 0 -> "$activeRulesCount regle(s) active(s)"
                    else -> "Non configure"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (canToggle) {
                    if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                } else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun NavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BadgedBox(badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("$badgeCount")
                    }
                }
            }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
