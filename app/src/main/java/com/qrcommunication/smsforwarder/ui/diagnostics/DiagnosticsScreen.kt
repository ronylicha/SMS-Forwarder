package com.qrcommunication.smsforwarder.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.ui.components.common.CheckSeverity
import com.qrcommunication.smsforwarder.ui.components.common.SettingsCard
import com.qrcommunication.smsforwarder.ui.components.common.StatusItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_diagnostics), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val errors = uiState.checks.count { it.severity == CheckSeverity.ERROR }
        val warnings = uiState.checks.count { it.severity == CheckSeverity.WARNING }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SummaryCard(errors = errors, warnings = warnings, total = uiState.checks.size)
            }
            item {
                SettingsCard(
                    title = stringResource(R.string.diag_audit_title),
                    icon = Icons.Filled.HealthAndSafety,
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            items(uiState.checks, key = { it.id }) { check ->
                SettingsCard {
                    StatusItem(
                        severity = check.severity,
                        title = check.title,
                        description = check.description,
                        actionLabel = check.fixLabel,
                        onAction = check.fixIntent?.let { intent ->
                            { runCatching { context.startActivity(intent) } }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(errors: Int, warnings: Int, total: Int) {
    val severity = when {
        errors > 0 -> CheckSeverity.ERROR
        warnings > 0 -> CheckSeverity.WARNING
        else -> CheckSeverity.OK
    }
    val message = when {
        errors > 0 -> "$errors probleme(s) bloquant(s) detecte(s)"
        warnings > 0 -> "$warnings avertissement(s) - le service peut etre limite"
        else -> stringResource(R.string.diag_all_pass, total, total)
    }
    SettingsCard {
        StatusItem(
            severity = severity,
            title = stringResource(R.string.diag_global_state),
            description = message,
        )
    }
}
