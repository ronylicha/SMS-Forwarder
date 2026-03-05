package com.qrcommunication.smsforwarder.domain.validator

import com.qrcommunication.smsforwarder.data.local.entity.FilterRule
import com.qrcommunication.smsforwarder.data.local.entity.FilterType
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.data.repository.FilterRepository
import com.qrcommunication.smsforwarder.util.PhoneValidator
import javax.inject.Inject
import javax.inject.Singleton

enum class FilterMode {
    NONE,       // Pas de filtre, tout passe
    WHITELIST,  // Seuls les numeros/mots-cles de la whitelist passent
    BLACKLIST;  // Les numeros/mots-cles de la blacklist sont bloques

    companion object {
        fun fromString(value: String): FilterMode {
            return entries.find { it.name == value } ?: NONE
        }
    }
}

data class FilterResult(
    val shouldForward: Boolean,
    val reason: String
)

@Singleton
class FilterEngine @Inject constructor(
    private val filterRepository: FilterRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend fun shouldForward(sender: String, content: String): FilterResult {
        val mode = FilterMode.fromString(preferencesManager.filterMode)

        if (mode == FilterMode.NONE) {
            return FilterResult(shouldForward = true, reason = "No filter active")
        }

        val activeRules = filterRepository.getActiveRules()
        if (activeRules.isEmpty()) {
            return FilterResult(shouldForward = true, reason = "No active rules")
        }

        val normalizedSender = PhoneValidator.normalize(sender)

        return when (mode) {
            FilterMode.WHITELIST -> {
                val whitelistRules = activeRules.filter { it.type == FilterType.WHITELIST.value }
                val matches = whitelistRules.any { matchesRule(it, normalizedSender, content) }
                if (matches) {
                    FilterResult(true, "Matches whitelist rule")
                } else {
                    FilterResult(false, "Not in whitelist")
                }
            }
            FilterMode.BLACKLIST -> {
                val blacklistRules = activeRules.filter { it.type == FilterType.BLACKLIST.value }
                val matches = blacklistRules.any { matchesRule(it, normalizedSender, content) }
                if (matches) {
                    FilterResult(false, "Matches blacklist rule")
                } else {
                    FilterResult(true, "Not in blacklist")
                }
            }
            FilterMode.NONE -> FilterResult(true, "No filter active")
        }
    }

    private fun matchesRule(rule: FilterRule, normalizedSender: String, content: String): Boolean {
        val pattern = rule.pattern.trim()
        // Verifier si le pattern correspond au numero (normalise)
        if (PhoneValidator.isValid(pattern)) {
            val normalizedPattern = PhoneValidator.normalize(pattern)
            return normalizedSender == normalizedPattern
        }
        // Sinon, traiter comme mot-cle (chercher dans sender + content)
        return normalizedSender.contains(pattern, ignoreCase = true) ||
               content.contains(pattern, ignoreCase = true)
    }
}
