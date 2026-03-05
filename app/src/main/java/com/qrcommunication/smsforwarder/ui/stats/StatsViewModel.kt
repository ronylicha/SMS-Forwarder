package com.qrcommunication.smsforwarder.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.domain.usecase.DailyStats
import com.qrcommunication.smsforwarder.domain.usecase.GetStatsUseCase
import com.qrcommunication.smsforwarder.domain.usecase.SmsStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val overallStats: SmsStats? = null,
    val dailyStats: List<DailyStats> = emptyList(),
    val isLoading: Boolean = true,
    val selectedPeriod: Int = 7
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observeOverallStats()
        loadDailyStats(7)
    }

    private fun observeOverallStats() {
        viewModelScope.launch {
            getStatsUseCase.getOverallStats().collect { stats ->
                _uiState.update {
                    it.copy(overallStats = stats, isLoading = false)
                }
            }
        }
    }

    fun loadDailyStats(days: Int) {
        viewModelScope.launch {
            val dailyStats = getStatsUseCase.getDailyStats(days)
            _uiState.update {
                it.copy(dailyStats = dailyStats)
            }
        }
    }

    fun setPeriod(days: Int) {
        _uiState.update { it.copy(selectedPeriod = days) }
        loadDailyStats(days)
    }
}
