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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.ui.components.PhoneNumberField
import com.qrcommunication.smsforwarder.ui.components.common.SettingsCard
import kotlin.math.abs

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

    // Toast confirmation: destination saved
    val destSavedMsg = stringResource(R.string.settings_destination_saved)
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(destSavedMsg)
            viewModel.clearSavedFlag()
        }
    }

    // Toast confirmation: test result
    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { result ->
            snackbarHostState.showSnackbar(result)
            viewModel.clearTestResult()
        }
    }

    // Language change: show toast then recreate activity
    val langChangedMsg = stringResource(R.string.settings_language_changed)
    LaunchedEffect(uiState.languageChanged) {
        if (uiState.languageChanged) {
            snackbarHostState.showSnackbar(langChangedMsg)
            viewModel.clearLanguageChangedFlag()
            // Recreate activity to apply new locale
            (context as? android.app.Activity)?.recreate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back)
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

            // ── Destination number ──
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
                        text = stringResource(R.string.settings_destination_title),
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
                            Text(stringResource(R.string.settings_save))
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
                                Text(stringResource(R.string.settings_send_test))
                            }
                        }
                    }
                }
            }

            // ── Language selector ──
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
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.settings_language_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_language_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LanguageOption(
                        label = stringResource(R.string.settings_language_system),
                        selected = uiState.appLanguage == "system",
                        onClick = { viewModel.setLanguage("system") }
                    )
                    LanguageOption(
                        label = stringResource(R.string.settings_language_french),
                        selected = uiState.appLanguage == "fr",
                        onClick = { viewModel.setLanguage("fr") }
                    )
                    LanguageOption(
                        label = stringResource(R.string.settings_language_english),
                        selected = uiState.appLanguage == "en",
                        onClick = { viewModel.setLanguage("en") }
                    )
                }
            }

            // ── Filtering ──
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
                            text = stringResource(R.string.settings_filter_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilterModeOption(
                        label = stringResource(R.string.settings_filter_none),
                        selected = uiState.filterMode == "NONE",
                        onClick = { viewModel.setFilterMode("NONE") }
                    )
                    FilterModeOption(
                        label = stringResource(R.string.settings_filter_whitelist),
                        selected = uiState.filterMode == "WHITELIST",
                        onClick = { viewModel.setFilterMode("WHITELIST") }
                    )
                    FilterModeOption(
                        label = stringResource(R.string.settings_filter_blacklist),
                        selected = uiState.filterMode == "BLACKLIST",
                        onClick = { viewModel.setFilterMode("BLACKLIST") }
                    )

                    HorizontalDivider()

                    FilledTonalButton(
                        onClick = onNavigateToFilters,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_manage_rules))
                    }
                }
            }

            // ── Dual SIM ──
            if (uiState.isDualSim) {
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
                                text = stringResource(R.string.settings_receiving_sim_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = stringResource(R.string.settings_receiving_sim_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterModeOption(
                            label = stringResource(R.string.settings_receiving_sim_all),
                            selected = uiState.receivingSimSlot == -1,
                            onClick = { viewModel.setReceivingSimSlot(-1) }
                        )
                        FilterModeOption(
                            label = stringResource(R.string.settings_receiving_sim_1_only),
                            selected = uiState.receivingSimSlot == 0,
                            onClick = { viewModel.setReceivingSimSlot(0) }
                        )
                        FilterModeOption(
                            label = stringResource(R.string.settings_receiving_sim_2_only),
                            selected = uiState.receivingSimSlot == 1,
                            onClick = { viewModel.setReceivingSimSlot(1) }
                        )
                    }
                }

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
                                text = stringResource(R.string.settings_sending_sim_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = stringResource(R.string.settings_sending_sim_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterModeOption(
                            label = stringResource(R.string.settings_sim_default),
                            selected = uiState.selectedSimSlot == -1,
                            onClick = { viewModel.setSimSlot(-1) }
                        )
                        FilterModeOption(
                            label = stringResource(R.string.settings_sim_1),
                            selected = uiState.selectedSimSlot == 0,
                            onClick = { viewModel.setSimSlot(0) }
                        )
                        FilterModeOption(
                            label = stringResource(R.string.settings_sim_2),
                            selected = uiState.selectedSimSlot == 1,
                            onClick = { viewModel.setSimSlot(1) }
                        )
                    }
                }
            }

            // ── Notification access ──
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

            // ── Third-party apps ──
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

            // ── Retry policy ──
            RetryPolicyCard(
                policy = uiState.retryPolicy,
                onMaxAttemptsChange = viewModel::updateRetryMaxAttempts,
                onInitialDelayChange = viewModel::updateRetryInitialDelay,
                onBackoffChange = viewModel::updateRetryBackoff,
            )

            // ── Advanced ──
            AdvancedNavigationCard(
                onNavigateToRules = onNavigateToRules,
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                onNavigateToNotifications = onNavigateToNotifications,
            )

            // ── About ──
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
                            text = stringResource(R.string.settings_about_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.settings_version),
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
                            text = stringResource(R.string.settings_application),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.settings_app_name),
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
private fun LanguageOption(
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
    SettingsCard(
        title = stringResource(R.string.settings_retry_title),
        icon = Icons.Filled.Cached,
    ) {
        Text(
            text = stringResource(R.string.settings_retry_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.settings_retry_max_attempts, policy.maxAttempts),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = policy.maxAttempts.toFloat(),
            onValueChange = { onMaxAttemptsChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
        )

        Text(
            text = stringResource(R.string.settings_retry_initial_delay),
            style = MaterialTheme.typography.bodyMedium
        )
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

        Text(
            text = stringResource(R.string.settings_retry_backoff),
            style = MaterialTheme.typography.bodyMedium
        )
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
            FilterChip(
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
            FilterChip(
                selected = abs(current - value) < 0.01,
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
    SettingsCard(
        title = stringResource(R.string.settings_advanced_title),
        icon = Icons.Filled.Tune,
    ) {
        FilledTonalButton(onClick = onNavigateToRules, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_rules_transfer))
        }
        FilledTonalButton(onClick = onNavigateToDiagnostics, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_diagnostics))
        }
        FilledTonalButton(onClick = onNavigateToNotifications, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_notification_center))
        }
    }
}
