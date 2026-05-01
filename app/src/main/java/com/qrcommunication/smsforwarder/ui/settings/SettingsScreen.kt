package com.qrcommunication.smsforwarder.ui.settings

import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.ui.components.PhoneNumberField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToAppWhitelist: () -> Unit = {},
    onNavigateToRules: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check notification access when returning from system settings
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Numero enregistre")
            viewModel.clearSavedFlag()
        }
    }

    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { result ->
            snackbarHostState.showSnackbar(result)
            viewModel.clearTestResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parametres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Destination number section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "Numero de destination",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    PhoneNumberField(
                        value = uiState.destinationNumber,
                        onValueChange = viewModel::updateDestination,
                        isValid = uiState.isNumberValid,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = viewModel::saveDestination,
                            enabled = uiState.isNumberValid,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enregistrer")
                        }

                        OutlinedButton(
                            onClick = viewModel::sendTestSms,
                            enabled = uiState.isNumberValid && !uiState.isTesting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Envoyer un test")
                            }
                        }
                    }
                }
            }

            // Filter section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Filtrage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilterModeOption(
                        label = "Aucun filtre (tout transferer)",
                        selected = uiState.filterMode == "NONE",
                        onClick = { viewModel.setFilterMode("NONE") }
                    )
                    FilterModeOption(
                        label = "Liste blanche (transferer seulement les filtres)",
                        selected = uiState.filterMode == "WHITELIST",
                        onClick = { viewModel.setFilterMode("WHITELIST") }
                    )
                    FilterModeOption(
                        label = "Liste noire (bloquer les filtres)",
                        selected = uiState.filterMode == "BLACKLIST",
                        onClick = { viewModel.setFilterMode("BLACKLIST") }
                    )

                    HorizontalDivider()

                    FilledTonalButton(
                        onClick = onNavigateToFilters,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gerer les regles de filtrage")
                    }
                }
            }

            // Dual SIM section
            if (uiState.isDualSim) {
                // SIM de reception (filtre)
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "SIM de reception",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Choisissez de quelles cartes SIM les SMS recus seront transferes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterModeOption(
                            label = "Toutes les SIM",
                            selected = uiState.receivingSimSlot == -1,
                            onClick = { viewModel.setReceivingSimSlot(-1) }
                        )
                        FilterModeOption(
                            label = "SIM 1 uniquement",
                            selected = uiState.receivingSimSlot == 0,
                            onClick = { viewModel.setReceivingSimSlot(0) }
                        )
                        FilterModeOption(
                            label = "SIM 2 uniquement",
                            selected = uiState.receivingSimSlot == 1,
                            onClick = { viewModel.setReceivingSimSlot(1) }
                        )
                    }
                }

                // SIM d'envoi
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "SIM d'envoi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Choisissez quelle carte SIM utiliser pour envoyer les SMS transferes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterModeOption(
                            label = "SIM par defaut",
                            selected = uiState.selectedSimSlot == -1,
                            onClick = { viewModel.setSimSlot(-1) }
                        )
                        FilterModeOption(
                            label = "SIM 1",
                            selected = uiState.selectedSimSlot == 0,
                            onClick = { viewModel.setSimSlot(0) }
                        )
                        FilterModeOption(
                            label = "SIM 2",
                            selected = uiState.selectedSimSlot == 1,
                            onClick = { viewModel.setSimSlot(1) }
                        )
                    }
                }
            }

            // Notification access section (RCS support)
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.settings_notification_access_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_notification_access_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.isNotificationAccessEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            Text(
                                text = if (uiState.isNotificationAccessEnabled) {
                                    stringResource(R.string.settings_notification_access_enabled)
                                } else {
                                    stringResource(R.string.settings_notification_access_disabled)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isNotificationAccessEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_notification_access_button))
                    }
                }
            }

            // Third-party apps section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.settings_app_whitelist_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_app_whitelist_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.isAppWhitelistEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = if (uiState.isAppWhitelistEnabled) {
                                    stringResource(R.string.settings_app_whitelist_status_active, uiState.appWhitelistCount)
                                } else {
                                    stringResource(R.string.settings_app_whitelist_status_inactive)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isAppWhitelistEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToAppWhitelist,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_app_whitelist_manage))
                    }
                }
            }

            // Retry policy section
            RetryPolicyCard(
                policy = uiState.retryPolicy,
                onMaxAttemptsChange = viewModel::updateRetryMaxAttempts,
                onInitialDelayChange = viewModel::updateRetryInitialDelay,
                onBackoffChange = viewModel::updateRetryBackoff,
            )

            // Advanced navigation section
            AdvancedNavigationCard(
                onNavigateToRules = onNavigateToRules,
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                onNavigateToNotifications = onNavigateToNotifications,
            )

            // About section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "A propos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Version",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.appVersion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Application",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "SMS Forwarder",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterModeOption(
    label: String,
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun RetryPolicyCard(
    policy: com.qrcommunication.smsforwarder.domain.model.RetryPolicy,
    onMaxAttemptsChange: (Int) -> Unit,
    onInitialDelayChange: (Long) -> Unit,
    onBackoffChange: (Double) -> Unit,
) {
    com.qrcommunication.smsforwarder.ui.components.common.SettingsCard(
        title = "Politique de retry",
        icon = Icons.Filled.Cached,
    ) {
        Text(
            text = "Comportement automatique en cas d'echec du transfert.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Tentatives max : ${policy.maxAttempts}",
            style = MaterialTheme.typography.bodyMedium,
        )
        androidx.compose.material3.Slider(
            value = policy.maxAttempts.toFloat(),
            onValueChange = { onMaxAttemptsChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
        )

        Text("Delai initial", style = MaterialTheme.typography.bodyMedium)
        DelayChips(
            current = policy.initialDelayMs,
            options = listOf(
                30_000L to "30s",
                60_000L to "1min",
                300_000L to "5min",
                900_000L to "15min",
            ),
            onSelect = onInitialDelayChange,
        )

        Text("Multiplicateur backoff", style = MaterialTheme.typography.bodyMedium)
        BackoffChips(
            current = policy.backoffMultiplier,
            options = listOf(1.5, 2.0, 3.0),
            onSelect = onBackoffChange,
        )
    }
}

@Composable
private fun DelayChips(
    current: Long,
    options: List<Pair<Long, String>>,
    onSelect: (Long) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            androidx.compose.material3.FilterChip(
                selected = current == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun BackoffChips(
    current: Double,
    options: List<Double>,
    onSelect: (Double) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { value ->
            androidx.compose.material3.FilterChip(
                selected = kotlin.math.abs(current - value) < 0.01,
                onClick = { onSelect(value) },
                label = { Text("x$value") },
            )
        }
    }
}

@Composable
private fun AdvancedNavigationCard(
    onNavigateToRules: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    com.qrcommunication.smsforwarder.ui.components.common.SettingsCard(
        title = "Avance",
        icon = Icons.Filled.Tune,
    ) {
        FilledTonalButton(onClick = onNavigateToRules, modifier = Modifier.fillMaxWidth()) {
            Text("Regles de transfert")
        }
        FilledTonalButton(onClick = onNavigateToDiagnostics, modifier = Modifier.fillMaxWidth()) {
            Text("Diagnostics")
        }
        FilledTonalButton(onClick = onNavigateToNotifications, modifier = Modifier.fillMaxWidth()) {
            Text("Centre de notifications")
        }
    }
}
