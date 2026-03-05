package com.qrcommunication.smsforwarder.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.domain.usecase.GetHistoryUseCase
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
    val totalCount: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val smsRepository: SmsRepository
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
            val flow = when {
                state.searchQuery.isNotBlank() -> getHistoryUseCase.searchRecords(state.searchQuery)
                state.selectedStatusFilter != null -> getHistoryUseCase.getRecordsByStatus(state.selectedStatusFilter)
                else -> getHistoryUseCase.getAllRecords()
            }

            flow.catch { _ ->
                _uiState.update { it.copy(isLoading = false) }
            }.collect { records ->
                _uiState.update {
                    it.copy(
                        records = records,
                        isLoading = false
                    )
                }
            }
        }
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
        _uiState.update {
            it.copy(
                searchQuery = query,
                selectedStatusFilter = null
            )
        }
        loadRecords()
    }

    fun filterByStatus(status: SmsStatus?) {
        _uiState.update {
            it.copy(
                selectedStatusFilter = status,
                searchQuery = ""
            )
        }
        loadRecords()
    }

    fun deleteAll() {
        viewModelScope.launch {
            smsRepository.deleteAllRecords()
        }
    }
}
