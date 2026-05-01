package com.qrcommunication.smsforwarder.domain.usecase

import com.qrcommunication.smsforwarder.data.local.entity.ForwardingRule
import com.qrcommunication.smsforwarder.data.repository.ForwardingRuleRepository
import javax.inject.Inject

/**
 * Trouve la premiere ForwardingRule (par priorite decroissante) qui match
 * un SMS donne sur sender et keyword.
 *
 * Patterns interpretes en regex (case-insensitive). Si pattern null = pas de filtre
 * sur ce critere. Une rule sans aucun pattern match TOUT.
 */
class MatchForwardingRuleUseCase @Inject constructor(
    private val ruleRepository: ForwardingRuleRepository,
) {
    suspend operator fun invoke(sender: String, content: String): ForwardingRule? {
        return ruleRepository.getEnabledOrdered().firstOrNull { rule ->
            matchesSender(rule, sender) && matchesKeyword(rule, content)
        }
    }

    private fun matchesSender(rule: ForwardingRule, sender: String): Boolean {
        val pattern = rule.senderPattern?.takeIf { it.isNotBlank() } ?: return true
        return runCatching {
            Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(sender)
        }.getOrElse { sender.contains(pattern, ignoreCase = true) }
    }

    private fun matchesKeyword(rule: ForwardingRule, content: String): Boolean {
        val pattern = rule.keywordPattern?.takeIf { it.isNotBlank() } ?: return true
        return runCatching {
            Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(content)
        }.getOrElse { content.contains(pattern, ignoreCase = true) }
    }
}
