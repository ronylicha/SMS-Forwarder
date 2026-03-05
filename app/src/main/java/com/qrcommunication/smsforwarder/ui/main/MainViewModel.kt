package com.qrcommunication.smsforwarder.ui.main

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.service.SmsForwardService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MainUiState(
    val isForwardingEnabled: Boolean = false,
    val destinationNumber: String = "",
    val smsForwardedCount: Int = 0,
    val isDestinationConfigured: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun toggleForwarding() {
        val currentState = _uiState.value
        if (!currentState.isDestinationConfigured) return

        val newEnabled = !currentState.isForwardingEnabled
        preferencesManager.isForwardingEnabled = newEnabled

        if (newEnabled) {
            startForwardingService()
        } else {
            stopForwardingService()
        }

        _uiState.update { it.copy(isForwardingEnabled = newEnabled) }
    }

    fun refreshState() {
        val destination = preferencesManager.destinationNumber
        _uiState.update {
            MainUiState(
                isForwardingEnabled = preferencesManager.isForwardingEnabled,
                destinationNumber = destination,
                smsForwardedCount = preferencesManager.smsForwardedCount,
                isDestinationConfigured = destination.isNotBlank()
            )
        }
    }

    private fun startForwardingService() {
        val intent = Intent(application, SmsForwardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    private fun stopForwardingService() {
        val intent = Intent(application, SmsForwardService::class.java).apply {
            action = SmsForwardService.ACTION_STOP_SERVICE
        }
        application.startService(intent)
    }
}
