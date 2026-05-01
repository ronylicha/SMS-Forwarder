package com.qrcommunication.smsforwarder.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repository: ForwardingRuleRepository,
) : ViewModel() {

    val rules: StateFlow<List<ForwardingRule>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleEnabled(rule: ForwardingRule) = viewModelScope.launch {
        repository.setEnabled(rule.id, !rule.isEnabled)
    }

    fun delete(rule: ForwardingRule) = viewModelScope.launch {
        repository.delete(rule)
    }
}
