package com.qrcommunication.smsforwarder.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.ui.components.common.ConfigOption
import com.qrcommunication.smsforwarder.ui.components.common.SettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: RuleEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMsg = stringResource(R.string.rule_edit_saved)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(savedMsg)
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTestResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.id == 0L) stringResource(R.string.rules_new) else stringResource(R.string.rule_edit_title_edit),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard(title = stringResource(R.string.rule_edit_identity), icon = Icons.Filled.Tune) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(stringResource(R.string.rule_edit_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.isEnabled, onCheckedChange = viewModel::updateEnabled)
                    Text(stringResource(R.string.rule_edit_active), modifier = Modifier.padding(start = 4.dp))
                }
            }

            SettingsCard(title = stringResource(R.string.rule_edit_match_criteria)) {
                OutlinedTextField(
                    value = uiState.senderPattern,
                    onValueChange = viewModel::updateSenderPattern,
                    label = { Text(stringResource(R.string.rule_edit_sender_pattern)) },
                    placeholder = { Text("ex: ^(\\+33|0)6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.keywordPattern,
                    onValueChange = viewModel::updateKeywordPattern,
                    label = { Text(stringResource(R.string.rule_edit_keyword_pattern)) },
                    placeholder = { Text("ex: code|otp|verification") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    stringResource(R.string.rule_edit_no_pattern_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsCard(title = stringResource(R.string.detail_destination)) {
                DestinationType.entries.forEach { type ->
                    ConfigOption(
                        label = type.toLabel(),
                        description = type.toHint(),
                        selected = uiState.destinationType == type,
                        onClick = { viewModel.updateDestinationType(type) },
                    )
                }
                OutlinedTextField(
                    value = uiState.destination,
                    onValueChange = viewModel::updateDestination,
                    label = { Text(uiState.destinationType.toFieldLabel()) },
                    placeholder = { Text(uiState.destinationType.toPlaceholder()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            SettingsCard(title = stringResource(R.string.rule_edit_test_example), icon = Icons.AutoMirrored.Filled.Send) {
                OutlinedTextField(
                    value = uiState.testSampleSender,
                    onValueChange = viewModel::updateTestSender,
                    label = { Text(stringResource(R.string.rule_edit_test_sender)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.testSampleContent,
                    onValueChange = viewModel::updateTestContent,
                    label = { Text(stringResource(R.string.rule_edit_test_content)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = viewModel::testRule, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.rule_edit_test_button))
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) stringResource(R.string.rule_edit_saving) else stringResource(R.string.rule_edit_save))
            }
        }
    }
}

@Composable
private fun DestinationType.toLabel(): String = when (this) {
    DestinationType.SMS -> stringResource(R.string.rule_edit_sms_type_label)
    DestinationType.WEBHOOK -> stringResource(R.string.rule_edit_webhook_type_label)
}

@Composable
private fun DestinationType.toHint(): String = when (this) {
    DestinationType.SMS -> stringResource(R.string.settings_destination_label)
    DestinationType.WEBHOOK -> stringResource(R.string.rule_edit_webhook_hint)
}

@Composable
private fun DestinationType.toFieldLabel(): String = when (this) {
    DestinationType.SMS -> stringResource(R.string.settings_destination_label)
    DestinationType.WEBHOOK -> stringResource(R.string.rule_edit_webhook_url_label)
}

private fun DestinationType.toPlaceholder(): String = when (this) {
    DestinationType.SMS -> "+33 6 12 34 56 78"
    DestinationType.WEBHOOK -> "https://example.com/sms-webhook"
}
