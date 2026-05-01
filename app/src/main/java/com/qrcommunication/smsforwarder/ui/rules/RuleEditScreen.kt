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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Regle enregistree")
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
                        if (uiState.id == 0L) "Nouvelle regle" else "Modifier la regle",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
            SettingsCard(title = "Identite", icon = Icons.Filled.Tune) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nom de la regle") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.isEnabled, onCheckedChange = viewModel::updateEnabled)
                    Text("Active", modifier = Modifier.padding(start = 4.dp))
                }
            }

            SettingsCard(title = "Critere de match") {
                OutlinedTextField(
                    value = uiState.senderPattern,
                    onValueChange = viewModel::updateSenderPattern,
                    label = { Text("Pattern expediteur (regex, optionnel)") },
                    placeholder = { Text("ex: ^(\\+33|0)6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.keywordPattern,
                    onValueChange = viewModel::updateKeywordPattern,
                    label = { Text("Mot-cle / regex contenu (optionnel)") },
                    placeholder = { Text("ex: code|otp|verification") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    "Une regle sans aucun pattern correspond a TOUS les SMS.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsCard(title = "Destination") {
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

            SettingsCard(title = "Tester avec un exemple", icon = Icons.AutoMirrored.Filled.Send) {
                OutlinedTextField(
                    value = uiState.testSampleSender,
                    onValueChange = viewModel::updateTestSender,
                    label = { Text("Expediteur de test") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.testSampleContent,
                    onValueChange = viewModel::updateTestContent,
                    label = { Text("Contenu de test") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = viewModel::testRule, modifier = Modifier.fillMaxWidth()) {
                    Text("Tester")
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Enregistrement..." else "Enregistrer la regle")
            }
        }
    }
}

private fun DestinationType.toLabel(): String = when (this) {
    DestinationType.SMS -> "SMS"
    DestinationType.WEBHOOK -> "Webhook (HTTP POST)"
}

private fun DestinationType.toHint(): String = when (this) {
    DestinationType.SMS -> "Renvoyer le message vers un numero de telephone"
    DestinationType.WEBHOOK -> "Envoyer un POST JSON a une URL"
}

private fun DestinationType.toFieldLabel(): String = when (this) {
    DestinationType.SMS -> "Numero de destination"
    DestinationType.WEBHOOK -> "URL du webhook"
}

private fun DestinationType.toPlaceholder(): String = when (this) {
    DestinationType.SMS -> "+33 6 12 34 56 78"
    DestinationType.WEBHOOK -> "https://example.com/sms-webhook"
}
