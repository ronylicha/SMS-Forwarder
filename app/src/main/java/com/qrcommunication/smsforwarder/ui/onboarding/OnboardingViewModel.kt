package com.qrcommunication.smsforwarder.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.service.SmsSender
import com.qrcommunication.smsforwarder.util.PhoneValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val destinationNumber: String = "",
    val isNumberValid: Boolean = false,
    val isSendingTest: Boolean = false,
    val testStatus: TestStatus = TestStatus.NOT_RUN,
    val testError: String? = null,
)

enum class TestStatus { NOT_RUN, SENT, FAILED }

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val smsSender: SmsSender,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateDestination(value: String) {
        val cleaned = value.trim()
        _uiState.update {
            it.copy(
                destinationNumber = cleaned,
                isNumberValid = PhoneValidator.isValid(cleaned),
                testStatus = TestStatus.NOT_RUN,
                testError = null,
            )
        }
    }

    fun sendTestSms() = viewModelScope.launch {
        val state = _uiState.value
        if (!state.isNumberValid) return@launch
        _uiState.update { it.copy(isSendingTest = true, testError = null) }
        val result = runCatching {
            smsSender.sendSms(
                state.destinationNumber,
                "[SMS Forwarder] Test d'envoi - le pipeline est operationnel.",
            )
        }
        _uiState.update {
            it.copy(
                isSendingTest = false,
                testStatus = if (result.isSuccess) TestStatus.SENT else TestStatus.FAILED,
                testError = result.exceptionOrNull()?.message,
            )
        }
    }

    fun finalize() {
        val state = _uiState.value
        if (state.isNumberValid) {
            preferencesManager.destinationNumber = state.destinationNumber
        }
        preferencesManager.isFirstLaunch = false
    }
}
