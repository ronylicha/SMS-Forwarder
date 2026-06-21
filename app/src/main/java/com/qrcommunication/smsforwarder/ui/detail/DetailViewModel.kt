package com.qrcommunication.smsforwarder.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.data.local.entity.SmsRecord
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.domain.usecase.GetHistoryUseCase
import com.qrcommunication.smsforwarder.domain.usecase.RetryResult
import com.qrcommunication.smsforwarder.domain.usecase.RetrySmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val record: SmsRecord? = null,
    val isLoading: Boolean = true,
    val retryResult: String? = null,
    val isRetrying: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val retrySmsUseCase: RetrySmsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadRecord(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val record = getHistoryUseCase.getRecordById(id)
            _uiState.update {
                it.copy(
                    record = record,
                    isLoading = false
                )
            }
        }
    }

    fun retrySms() {
        val record = _uiState.value.record ?: return
        if (record.status != SmsStatus.FAILED.value) return

        _uiState.update { it.copy(isRetrying = true, retryResult = null) }

        viewModelScope.launch {
            val result = retrySmsUseCase(record.id)
            val message = when (result) {
                is RetryResult.Success -> context.getString(R.string.retry_success)
                is RetryResult.Failed -> context.getString(R.string.retry_failed_short, result.error)
                is RetryResult.NotFound -> context.getString(R.string.detail_not_found)
                is RetryResult.MaxRetriesReached -> context.getString(R.string.detail_retry_max_reached)
            }

            _uiState.update { it.copy(isRetrying = false, retryResult = message) }

            if (result is RetryResult.Success) {
                loadRecord(record.id)
            }
        }
    }

    fun clearRetryResult() {
        _uiState.update { it.copy(retryResult = null) }
    }
}
