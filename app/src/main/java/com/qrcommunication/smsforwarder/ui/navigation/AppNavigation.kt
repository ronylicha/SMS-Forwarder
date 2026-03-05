package com.qrcommunication.smsforwarder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.ui.components.PermissionHandler
import com.qrcommunication.smsforwarder.ui.detail.DetailScreen
import com.qrcommunication.smsforwarder.ui.filter.FilterScreen
import com.qrcommunication.smsforwarder.ui.history.HistoryScreen
import com.qrcommunication.smsforwarder.ui.main.MainScreen
import com.qrcommunication.smsforwarder.ui.onboarding.OnboardingScreen
import com.qrcommunication.smsforwarder.ui.settings.SettingsScreen
import com.qrcommunication.smsforwarder.ui.stats.StatsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val preferencesManager = remember {
        PreferencesManager(context)
    }
    var permissionsGranted by remember { mutableStateOf(false) }

    if (!permissionsGranted) {
        PermissionHandler(
            onAllPermissionsGranted = { permissionsGranted = true },
            onPermissionDenied = { permissionsGranted = true } // Continuer meme si refuse
        )
        return
    }

    val startDestination = if (preferencesManager.isFirstLaunch) {
        Screen.Onboarding.route
    } else {
        Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    preferencesManager.isFirstLaunch = false
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFilters = {
                    navController.navigate(Screen.Filter.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { smsId ->
                    navController.navigate(Screen.Detail().createRoute(smsId))
                }
            )
        }

        composable(
            route = Screen.Detail().route,
            arguments = listOf(
                navArgument(Screen.Detail.SMS_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val smsId = backStackEntry.arguments?.getLong(Screen.Detail.SMS_ID_ARG) ?: 0L
            DetailScreen(
                smsId = smsId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Filter.route) {
            FilterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
