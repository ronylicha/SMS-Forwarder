package com.qrcommunication.smsforwarder.ui.navigation

sealed class Screen(val route: String) {

    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object Filter : Screen("filter")
    data object Stats : Screen("stats")
    data object Onboarding : Screen("onboarding")
    data object AppWhitelist : Screen("app_whitelist")
    data object Rules : Screen("rules")
    data object Diagnostics : Screen("diagnostics")
    data object NotificationCenter : Screen("notification_center")

    data class RuleEdit(val ruleId: Long = 0L) : Screen("rule_edit/{ruleId}") {
        fun createRoute(id: Long): String = "rule_edit/$id"
        companion object {
            const val RULE_ID_ARG = "ruleId"
        }
    }

    data class Detail(val smsId: Long = 0L) : Screen("detail/{smsId}") {
        fun createRoute(smsId: Long): String = "detail/$smsId"

        companion object {
            const val SMS_ID_ARG = "smsId"
        }
    }
}
