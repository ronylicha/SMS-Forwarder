package com.qrcommunication.smsforwarder.ui.settings

import android.app.Application
import android.provider.Settings
import android.telephony.SubscriptionManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.domain.model.RetryPolicy
import com.qrcommunication.smsforwarder.service.SmsSender
import com.qrcommunication.smsforwarder.util.LocaleManager
import com.qrcommunication.smsforwarder.util.PhoneValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val destinationNumber: String = "",
    val isNumberValid: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val filterMode: String = "NONE",
    val selectedSimSlot: Int = -1,
    val receivingSimSlot: Int = -1,
    val isDualSim: Boolean = false,
    val isSaved: Boolean = false,
    val appVersion: String = "1.0.0",
    val isNotificationAccessEnabled: Boolean = false,
    val isAppWhitelistEnabled: Boolean = false,
    val appWhitelistCount: Int = 0,
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val appLanguage: String = "system",
    val languageChanged: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager,
    private val smsSender: SmsSender
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkNotificationAccess()
    }

    private fun loadSettings() {
        val destination = preferencesManager.destinationNumber
        val isDualSim = checkDualSim()
        val version = try {
            application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

        _uiState.update {
            SettingsUiState(
                destinationNumber = destination,
                isNumberValid = destination.isNotBlank() && PhoneValidator.isValid(destination),
                filterMode = preferencesManager.filterMode,
                selectedSimSlot = preferencesManager.selectedSimSlot,
                receivingSimSlot = preferencesManager.receivingSimSlot,
                isDualSim = isDualSim,
                appVersion = version,
                isAppWhitelistEnabled = preferencesManager.isAppWhitelistEnabled,
                appWhitelistCount = preferencesManager.appWhitelistPackages.size,
                retryPolicy = preferencesManager.retryPolicy,
                appLanguage = preferencesManager.appLanguage,
            )
        }
    }

    fun updateRetryMaxAttempts(value: Int) = updateRetryPolicy {
        it.copy(maxAttempts = value.coerceIn(1, 10))
    }

    fun updateRetryInitialDelay(ms: Long) = updateRetryPolicy {
        it.copy(initialDelayMs = ms.coerceAtLeast(1_000L))
    }

    fun updateRetryBackoff(value: Double) = updateRetryPolicy {
        it.copy(backoffMultiplier = value.coerceIn(1.0, 5.0))
    }

    private fun updateRetryPolicy(transform: (RetryPolicy) -> RetryPolicy) {
        val current = _uiState.value.retryPolicy
        val updated = transform(current)
        preferencesManager.retryPolicy = updated
        _uiState.update { it.copy(retryPolicy = updated) }
    }

    fun updateDestination(number: String) {
        _uiState.update {
            it.copy(
                destinationNumber = number,
                isNumberValid = number.isNotBlank() && PhoneValidator.isValid(number),
                isSaved = false,
                testResult = null
            )
        }
    }

    fun saveDestination() {
        val state = _uiState.value
        if (!state.isNumberValid) return

        val normalized = PhoneValidator.normalize(state.destinationNumber)
        preferencesManager.destinationNumber = normalized
        _uiState.update {
            it.copy(
                destinationNumber = normalized,
                isSaved = true,
                testResult = null
            )
        }
    }

    fun setLanguage(language: String) {
        LocaleManager.setLanguage(application, language)
        preferencesManager.appLanguage = language
        _uiState.update {
            it.copy(
                appLanguage = language,
                languageChanged = true,
            )
        }
    }

    fun clearLanguageChangedFlag() {
        _uiState.update { it.copy(languageChanged = false) }
    }

    fun sendTestSms() {
        val state = _uiState.value
        if (!state.isNumberValid) return

        _uiState.update { it.copy(isTesting = true, testResult = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destination = PhoneValidator.normalize(state.destinationNumber)
                val testMsg = application.getString(R.string.settings_test_message)
                smsSender.sendSms(destination, testMsg)
                val successMsg = application.getString(R.string.settings_test_success)
                _uiState.update {
                    it.copy(isTesting = false, testResult = successMsg)
                }
            } catch (e: Exception) {
                val failMsg = application.getString(R.string.settings_test_failed, e.message ?: "")
                _uiState.update {
                    it.copy(isTesting = false, testResult = failMsg)
                }
            }
        }
    }

    fun setFilterMode(mode: String) {
        preferencesManager.filterMode = mode
        _uiState.update { it.copy(filterMode = mode) }
    }

    fun setSimSlot(slot: Int) {
        preferencesManager.selectedSimSlot = slot
        _uiState.update { it.copy(selectedSimSlot = slot) }
    }

    fun setReceivingSimSlot(slot: Int) {
        preferencesManager.receivingSimSlot = slot
        _uiState.update { it.copy(receivingSimSlot = slot) }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun checkNotificationAccess() {
        val enabledListeners = Settings.Secure.getString(
            application.contentResolver,
            "enabled_notification_listeners"
        )
        val isEnabled = enabledListeners?.contains(application.packageName) == true
        _uiState.update {
            it.copy(
                isNotificationAccessEnabled = isEnabled,
                isAppWhitelistEnabled = preferencesManager.isAppWhitelistEnabled,
                appWhitelistCount = preferencesManager.appWhitelistPackages.size
            )
        }
    }

    private fun checkDualSim(): Boolean {
        return try {
            val subscriptionManager = application.getSystemService(SubscriptionManager::class.java)
            val activeSubscriptions = subscriptionManager?.activeSubscriptionInfoCount ?: 0
            activeSubscriptions > 1
        } catch (_: SecurityException) {
            false
        }
    }
}
