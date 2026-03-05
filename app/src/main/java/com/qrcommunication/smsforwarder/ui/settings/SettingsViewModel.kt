package com.qrcommunication.smsforwarder.ui.settings

import android.app.Application
import android.provider.Settings
import android.telephony.SubscriptionManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.service.SmsSender
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
    val isDualSim: Boolean = false,
    val isSaved: Boolean = false,
    val appVersion: String = "1.0.0",
    val isNotificationAccessEnabled: Boolean = false
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
                isDualSim = isDualSim,
                appVersion = version
            )
        }
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

    fun sendTestSms() {
        val state = _uiState.value
        if (!state.isNumberValid) return

        _uiState.update { it.copy(isTesting = true, testResult = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destination = PhoneValidator.normalize(state.destinationNumber)
                smsSender.sendSms(destination, "[SMS Forwarder] Ceci est un SMS de test.")
                _uiState.update {
                    it.copy(isTesting = false, testResult = "SMS de test envoye avec succes")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTesting = false, testResult = "Echec de l'envoi : ${e.message}")
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
        _uiState.update { it.copy(isNotificationAccessEnabled = isEnabled) }
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
