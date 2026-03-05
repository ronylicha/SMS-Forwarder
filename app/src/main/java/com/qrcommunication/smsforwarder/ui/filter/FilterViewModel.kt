package com.qrcommunication.smsforwarder.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.domain.usecase.ManageFiltersUseCase
import com.qrcommunication.smsforwarder.domain.validator.FilterMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterUiState(
    val rules: List<FilterRule> = emptyList(),
    val currentMode: FilterMode = FilterMode.NONE,
    val newPattern: String = "",
    val selectedType: FilterType = FilterType.WHITELIST,
    val isLoading: Boolean = true
)

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val manageFiltersUseCase: ManageFiltersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMode()
        observeRules()
    }

    private fun loadCurrentMode() {
        val mode = manageFiltersUseCase.getCurrentMode()
        _uiState.update { it.copy(currentMode = mode) }
    }

    private fun observeRules() {
        viewModelScope.launch {
            manageFiltersUseCase.getAllRules().collect { rules ->
                _uiState.update {
                    it.copy(rules = rules, isLoading = false)
                }
            }
        }
    }

    fun setMode(mode: FilterMode) {
        manageFiltersUseCase.setFilterMode(mode)
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun updateNewPattern(pattern: String) {
        _uiState.update { it.copy(newPattern = pattern) }
    }

    fun setSelectedType(type: FilterType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun addRule() {
        val state = _uiState.value
        val pattern = state.newPattern.trim()
        if (pattern.isBlank()) return

        viewModelScope.launch {
            manageFiltersUseCase.addRule(pattern, state.selectedType)
            _uiState.update { it.copy(newPattern = "") }
        }
    }

    fun toggleRule(rule: FilterRule) {
        viewModelScope.launch {
            manageFiltersUseCase.toggleRule(rule)
        }
    }

    fun deleteRule(rule: FilterRule) {
        viewModelScope.launch {
            manageFiltersUseCase.deleteRule(rule)
        }
    }
}
