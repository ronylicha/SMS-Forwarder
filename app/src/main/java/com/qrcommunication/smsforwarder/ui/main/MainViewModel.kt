package com.qrcommunication.smsforwarder.ui.main

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.SmsStatus
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.AppNotificationRepository
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.service.SmsForwardService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class MainUiState(
    val isForwardingEnabled: Boolean = false,
    val destinationNumber: String = "",
    val isDestinationConfigured: Boolean = false,
    val totalForwarded: Int = 0,
    val totalFailed: Int = 0,
    val totalPending: Int = 0,
    val sentLast24h: Int = 0,
    val failedLast24h: Int = 0,
    val activeRulesCount: Int = 0,
    val unreadNotifications: Int = 0,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager,
    private val smsRepository: SmsRepository,
    ruleRepository: ForwardingRuleRepository,
    notificationRepository: AppNotificationRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(loadBaseState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeCounts(SmsStatus.SENT) { state, value -> state.copy(totalForwarded = value) }
        observeCounts(SmsStatus.FAILED) { state, value -> state.copy(totalFailed = value) }
        observeCounts(SmsStatus.PENDING) { state, value -> state.copy(totalPending = value) }

        viewModelScope.launch {
            notificationRepository.observeUnreadCount().collect { count ->
                _uiState.update { it.copy(unreadNotifications = count) }
            }
        }
        viewModelScope.launch {
            ruleRepository.observeAll().collect { rules ->
                _uiState.update { it.copy(activeRulesCount = rules.count { rule -> rule.isEnabled }) }
            }
        }
        loadLast24h()
    }

    private fun observeCounts(
        status: SmsStatus,
        reducer: (MainUiState, Int) -> MainUiState,
    ) = viewModelScope.launch {
        smsRepository.getRecordCountByStatus(status).collect { count ->
            _uiState.update { reducer(it, count) }
        }
    }

    private fun loadBaseState(): MainUiState {
        val destination = preferencesManager.destinationNumber
        return MainUiState(
            isForwardingEnabled = preferencesManager.isForwardingEnabled,
            destinationNumber = destination,
            isDestinationConfigured = destination.isNotBlank(),
        )
    }

    private fun loadLast24h() = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val start = now - TimeUnit.HOURS.toMillis(24)
        val records = withContext(Dispatchers.IO) {
            smsRepository.getRecordsForDateRange(start, now)
        }
        val sent = records.count { it.status == SmsStatus.SENT.value }
        val failed = records.count { it.status == SmsStatus.FAILED.value }
        _uiState.update { it.copy(sentLast24h = sent, failedLast24h = failed) }
    }

    fun toggleForwarding() {
        val current = _uiState.value
        if (!current.isDestinationConfigured) return
        val newEnabled = !current.isForwardingEnabled
        preferencesManager.isForwardingEnabled = newEnabled
        if (newEnabled) startForwardingService() else stopForwardingService()
        _uiState.update { it.copy(isForwardingEnabled = newEnabled) }
    }

    fun refreshState() {
        val base = loadBaseState()
        _uiState.update { it.copy(
            isForwardingEnabled = base.isForwardingEnabled,
            destinationNumber = base.destinationNumber,
            isDestinationConfigured = base.isDestinationConfigured,
        ) }
        loadLast24h()
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
