package com.qrcommunication.smsforwarder.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.ui.components.SmsListItem
import com.qrcommunication.smsforwarder.ui.components.common.EmptyState
import com.qrcommunication.smsforwarder.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.retryFeedback) {
        uiState.retryFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRetryFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Historique", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Filtrer par date",
                            tint = if (uiState.dateRangeStart != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (uiState.totalCount > 0) {
                        Badge(
                            modifier = Modifier.padding(end = 16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text("${uiState.totalCount}", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            SearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::search,
            )

            StatusFilterRow(
                selected = uiState.selectedStatusFilter,
                onSelect = viewModel::filterByStatus,
            )

            DestinationFilterRow(
                selected = uiState.selectedDestinationFilter,
                onSelect = viewModel::filterByDestination,
            )

            DateRangeBadge(
                startMs = uiState.dateRangeStart,
                endMs = uiState.dateRangeEnd,
                onClear = viewModel::clearDateRange,
            )

            HistoryContent(
                isLoading = uiState.isLoading,
                records = uiState.records,
                searchQuery = uiState.searchQuery,
                onItemClick = onNavigateToDetail,
                onRetry = viewModel::retry,
            )
        }
    }

    if (showDatePicker) {
        DateRangeDialog(
            initialStart = uiState.dateRangeStart,
            initialEnd = uiState.dateRangeEnd,
            onConfirm = { start, end ->
                viewModel.filterByDateRange(start, end)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun DateRangeBadge(startMs: Long?, endMs: Long?, onClear: () -> Unit) {
    if (startMs == null && endMs == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        val text = buildString {
            append(startMs?.let { DateFormatter.formatDateOnly(it) } ?: "...")
            append(" - ")
            append(endMs?.let { DateFormatter.formatDateOnly(it) } ?: "...")
        }
        Text(text, style = MaterialTheme.typography.labelMedium)
        TextButton(onClick = onClear) { Text("Effacer") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    initialStart: Long?,
    initialEnd: Long?,
    onConfirm: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart,
        initialSelectedEndDateMillis = initialEnd,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(state.selectedStartDateMillis, state.selectedEndDateMillis) },
            ) { Text("Appliquer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    ) {
        DateRangePicker(state = state, modifier = Modifier.padding(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Rechercher par expediteur, contenu, destination...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Effacer")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun StatusFilterRow(selected: SmsStatus?, onSelect: (SmsStatus?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { StatusChip("Tous", selected == null) { onSelect(null) } }
        item { StatusChip("Envoyes", selected == SmsStatus.SENT) { onSelect(SmsStatus.SENT) } }
        item { StatusChip("Echoues", selected == SmsStatus.FAILED) { onSelect(SmsStatus.FAILED) } }
        item { StatusChip("En attente", selected == SmsStatus.PENDING) { onSelect(SmsStatus.PENDING) } }
        item { StatusChip("Filtres", selected == SmsStatus.FILTERED) { onSelect(SmsStatus.FILTERED) } }
    }
}

@Composable
private fun DestinationFilterRow(
    selected: DestinationType?,
    onSelect: (DestinationType?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { StatusChip("Toutes destinations", selected == null) { onSelect(null) } }
        DestinationType.entries.forEach { type ->
            item { StatusChip(type.name, selected == type) { onSelect(type) } }
        }
    }
}

@Composable
private fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun HistoryContent(
    isLoading: Boolean,
    records: List<SmsRecord>,
    searchQuery: String,
    onItemClick: (Long) -> Unit,
    onRetry: (Long) -> Unit,
) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        records.isEmpty() -> EmptyState(
            icon = Icons.Outlined.Inbox,
            title = if (searchQuery.isNotBlank()) "Aucun resultat pour \"$searchQuery\""
            else "Aucun SMS transfere",
        )

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(records, key = { it.id }) { record ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SmsListItem(
                        record = record,
                        onClick = { onItemClick(record.id) },
                        modifier = Modifier.weight(1f),
                    )
                    if (record.status == SmsStatus.FAILED.value) {
                        IconButton(onClick = { onRetry(record.id) }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Renvoyer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
