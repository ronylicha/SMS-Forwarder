package com.qrcommunication.smsforwarder.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.util.DiagnosticCheck
import com.qrcommunication.smsforwarder.util.DiagnosticsRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsUiState(
    val checks: List<DiagnosticCheck> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val runner: DiagnosticsRunner,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val checks = runner.runAll()
            _uiState.update { DiagnosticsUiState(checks = checks, isLoading = false) }
        }
    }
}
