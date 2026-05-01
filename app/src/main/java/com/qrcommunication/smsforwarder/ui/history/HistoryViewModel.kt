package com.qrcommunication.smsforwarder.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.usecase.GetHistoryUseCase
import com.qrcommunication.smsforwarder.domain.usecase.RetryResult
import com.qrcommunication.smsforwarder.domain.usecase.RetrySmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val records: List<SmsRecord> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedStatusFilter: SmsStatus? = null,
    val selectedDestinationFilter: DestinationType? = null,
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val totalCount: Int = 0,
    val retryFeedback: String? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val smsRepository: SmsRepository,
    private val retrySms: RetrySmsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var collectionJob: Job? = null

    init {
        loadRecords()
        observeTotalCount()
    }

    private fun loadRecords() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val baseFlow = when {
                state.searchQuery.isNotBlank() -> getHistoryUseCase.searchRecords(state.searchQuery)
                state.selectedStatusFilter != null -> getHistoryUseCase.getRecordsByStatus(state.selectedStatusFilter)
                else -> getHistoryUseCase.getAllRecords()
            }

            baseFlow.catch { _ ->
                _uiState.update { it.copy(isLoading = false) }
            }.collect { records ->
                val filtered = records
                    .let { applyDestinationFilter(it, state.selectedDestinationFilter) }
                    .let { applyDateRangeFilter(it, state.dateRangeStart, state.dateRangeEnd) }
                _uiState.update { it.copy(records = filtered, isLoading = false) }
            }
        }
    }

    private fun applyDestinationFilter(
        records: List<SmsRecord>,
        filter: DestinationType?,
    ): List<SmsRecord> {
        if (filter == null) return records
        return records.filter { inferDestinationType(it.destination) == filter }
    }

    private fun applyDateRangeFilter(
        records: List<SmsRecord>,
        startMs: Long?,
        endMs: Long?,
    ): List<SmsRecord> {
        if (startMs == null && endMs == null) return records
        val effectiveStart = startMs ?: Long.MIN_VALUE
        val effectiveEnd = endMs ?: Long.MAX_VALUE
        return records.filter { it.receivedAt in effectiveStart..effectiveEnd }
    }

    /**
     * Heuristique : URL avec scheme = WEBHOOK, sinon SMS.
     * On evite ainsi une migration DB pour stocker explicitement le type.
     */
    private fun inferDestinationType(destination: String): DestinationType = when {
        destination.contains("://") -> DestinationType.WEBHOOK
        else -> DestinationType.SMS
    }

    private fun observeTotalCount() {
        viewModelScope.launch {
            getHistoryUseCase.getRecordCount()
                .catch { /* ignore */ }
                .collect { count ->
                    _uiState.update { it.copy(totalCount = count) }
                }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query, selectedStatusFilter = null) }
        loadRecords()
    }

    fun filterByStatus(status: SmsStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status, searchQuery = "") }
        loadRecords()
    }

    fun filterByDestination(type: DestinationType?) {
        _uiState.update { it.copy(selectedDestinationFilter = type) }
        loadRecords()
    }

    fun filterByDateRange(startMs: Long?, endMs: Long?) {
        _uiState.update { it.copy(dateRangeStart = startMs, dateRangeEnd = endMs) }
        loadRecords()
    }

    fun clearDateRange() = filterByDateRange(null, null)

    fun retry(recordId: Long) = viewModelScope.launch {
        val message = when (val result = retrySms(recordId)) {
            RetryResult.Success -> "Renvoye avec succes"
            RetryResult.NotFound -> "SMS introuvable"
            RetryResult.MaxRetriesReached -> "Nombre maximum de tentatives atteint"
            is RetryResult.Failed -> "Echec : ${result.error}"
        }
        _uiState.update { it.copy(retryFeedback = message) }
    }

    fun clearRetryFeedback() = _uiState.update { it.copy(retryFeedback = null) }

    fun deleteAll() = viewModelScope.launch { smsRepository.deleteAllRecords() }
}
