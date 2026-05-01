package com.qrcommunication.smsforwarder.ui.rules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.DestinationType
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import com.qrcommunication.smsforwarder.domain.usecase.MatchForwardingRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RuleEditUiState(
    val id: Long = 0L,
    val name: String = "",
    val priority: Int = 0,
    val senderPattern: String = "",
    val keywordPattern: String = "",
    val destinationType: DestinationType = DestinationType.SMS,
    val destination: String = "",
    val isEnabled: Boolean = true,
    val testSampleSender: String = "",
    val testSampleContent: String = "",
    val testResult: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class RuleEditViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: ForwardingRuleRepository,
    private val matchRule: MatchForwardingRuleUseCase,
) : ViewModel() {

    private val ruleId: Long = savedState["ruleId"] ?: 0L

    private val _uiState = MutableStateFlow(RuleEditUiState())
    val uiState: StateFlow<RuleEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        if (ruleId == 0L) {
            _uiState.update { it.copy(isLoading = false) }
            return@launch
        }
        val rule = repository.getById(ruleId)
        if (rule == null) {
            _uiState.update { it.copy(isLoading = false) }
            return@launch
        }
        _uiState.update {
            RuleEditUiState(
                id = rule.id,
                name = rule.name,
                priority = rule.priority,
                senderPattern = rule.senderPattern.orEmpty(),
                keywordPattern = rule.keywordPattern.orEmpty(),
                destinationType = DestinationType.fromValue(rule.destinationType),
                destination = rule.destination,
                isEnabled = rule.isEnabled,
                isLoading = false,
            )
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updatePriority(value: Int) = _uiState.update { it.copy(priority = value) }
    fun updateSenderPattern(value: String) = _uiState.update { it.copy(senderPattern = value) }
    fun updateKeywordPattern(value: String) = _uiState.update { it.copy(keywordPattern = value) }
    fun updateDestinationType(value: DestinationType) =
        _uiState.update { it.copy(destinationType = value) }
    fun updateDestination(value: String) = _uiState.update { it.copy(destination = value) }
    fun updateEnabled(value: Boolean) = _uiState.update { it.copy(isEnabled = value) }
    fun updateTestSender(value: String) = _uiState.update { it.copy(testSampleSender = value) }
    fun updateTestContent(value: String) = _uiState.update { it.copy(testSampleContent = value) }

    fun testRule() = viewModelScope.launch {
        val state = _uiState.value
        val match = matchRule(state.testSampleSender, state.testSampleContent)
        val result = when {
            match == null -> "Aucune regle ne match cet exemple"
            match.id == state.id -> "Cette regle match"
            else -> "Une autre regle (priorite plus haute) match : '${match.name}'"
        }
        _uiState.update { it.copy(testResult = result) }
    }

    fun clearTestResult() = _uiState.update { it.copy(testResult = null) }

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        if (state.destination.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(testResult = "Nom et destination obligatoires") }
            return@launch
        }
        _uiState.update { it.copy(isSaving = true) }
        val rule = ForwardingRule(
            id = state.id,
            name = state.name.trim(),
            priority = state.priority,
            senderPattern = state.senderPattern.takeIf { it.isNotBlank() },
            keywordPattern = state.keywordPattern.takeIf { it.isNotBlank() },
            destinationType = state.destinationType.name,
            destination = state.destination.trim(),
            isEnabled = state.isEnabled,
            createdAt = 0L,
            updatedAt = 0L,
        )
        repository.upsert(rule)
        _uiState.update { it.copy(isSaving = false, isSaved = true) }
    }
}
