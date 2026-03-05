package com.qrcommunication.smsforwarder.ui.navigation

sealed class Screen(val route: String) {

    data object Main : Screen("main")

    data object Settings : Screen("settings")

    data object History : Screen("history")

    data object Filter : Screen("filter")

    data object Stats : Screen("stats")

    data object Onboarding : Screen("onboarding")

    data class Detail(val smsId: Long = 0L) : Screen("detail/{smsId}") {
        fun createRoute(smsId: Long): String = "detail/$smsId"

        companion object {
            const val SMS_ID_ARG = "smsId"
        }
    }
}
