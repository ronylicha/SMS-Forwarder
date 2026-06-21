package com.qrcommunication.smsforwarder.ui.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.domain.validator.FilterMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit,
    viewModel: FilterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filter_title)) },
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
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Filter mode section
                item {
                    FilterModeSection(
                        currentMode = uiState.currentMode,
                        onModeSelected = viewModel::setMode
                    )
                }

                // Add rule section + rules list (visible only when mode != NONE)
                if (uiState.currentMode != FilterMode.NONE) {
                    item {
                        AddRuleSection(
                            newPattern = uiState.newPattern,
                            selectedType = uiState.selectedType,
                            onPatternChange = viewModel::updateNewPattern,
                            onTypeChange = viewModel::setSelectedType,
                            onAddRule = viewModel::addRule
                        )
                    }

                    item {
                        Text(
                            text = stringResource(R.string.filter_rules_active),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.rules.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FilterList,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.filter_no_rules),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.filter_no_rules_hint),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.rules,
                            key = { it.id }
                        ) { rule ->
                            FilterRuleItem(
                                rule = rule,
                                onToggle = { viewModel.toggleRule(rule) },
                                onDelete = { viewModel.deleteRule(rule) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterModeSection(
    currentMode: FilterMode,
    onModeSelected: (FilterMode) -> Unit
) {
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
                text = stringResource(R.string.filter_mode_title_short),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val modes = listOf(
                    FilterMode.NONE to stringResource(R.string.filter_mode_none),
                    FilterMode.WHITELIST to stringResource(R.string.filter_whitelist),
                    FilterMode.BLACKLIST to stringResource(R.string.filter_blacklist)
                )

                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = currentMode == mode,
                        onClick = { onModeSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = modes.size
                        )
                    ) {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text(
                text = when (currentMode) {
                    FilterMode.NONE -> stringResource(R.string.filter_none_desc_short)
                    FilterMode.WHITELIST -> stringResource(R.string.filter_whitelist_desc)
                    FilterMode.BLACKLIST -> stringResource(R.string.filter_blacklist_desc)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRuleSection(
    newPattern: String,
    selectedType: FilterType,
    onPatternChange: (String) -> Unit,
    onTypeChange: (FilterType) -> Unit,
    onAddRule: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
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
                    text = stringResource(R.string.filter_add_rule),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newPattern,
                    onValueChange = onPatternChange,
                    label = { Text(stringResource(R.string.filter_pattern_label)) },
                    placeholder = { Text(stringResource(R.string.filter_pattern_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf(
                        FilterType.WHITELIST to stringResource(R.string.filter_whitelist),
                        FilterType.BLACKLIST to stringResource(R.string.filter_blacklist)
                    )

                    types.forEachIndexed { index, (type, label) ->
                        SegmentedButton(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = types.size
                            )
                        ) {
                            Text(label)
                        }
                    }
                }

                Button(
                    onClick = onAddRule,
                    enabled = newPattern.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.filter_add_button))
                }
            }
        }
    }
}

@Composable
private fun FilterRuleItem(
    rule: FilterRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val filterType = try {
                    FilterType.fromValue(rule.type)
                } catch (_: IllegalArgumentException) {
                    null
                }

                filterType?.let { type ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (type) {
                                    FilterType.WHITELIST -> stringResource(R.string.filter_whitelist)
                                    FilterType.BLACKLIST -> stringResource(R.string.filter_blacklist)
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (type) {
                                FilterType.WHITELIST -> MaterialTheme.colorScheme.primaryContainer
                                FilterType.BLACKLIST -> MaterialTheme.colorScheme.errorContainer
                            },
                            labelColor = when (type) {
                                FilterType.WHITELIST -> MaterialTheme.colorScheme.onPrimaryContainer
                                FilterType.BLACKLIST -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    )
                }
            }

            Switch(
                checked = rule.isActive,
                onCheckedChange = { onToggle() }
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
