package com.qrcommunication.smsforwarder.ui.appwhitelist

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.preferences.PreferencesManager
import com.qrcommunication.smsforwarder.service.NotificationInterceptorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isInstalled: Boolean = true
)

data class AppWhitelistUiState(
    val isEnabled: Boolean = false,
    val whitelistedApps: List<InstalledApp> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val showPicker: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AppWhitelistViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppWhitelistUiState())
    val uiState: StateFlow<AppWhitelistUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            val isEnabled = preferencesManager.isAppWhitelistEnabled
            val whitelistedPackages = preferencesManager.appWhitelistPackages
            val whitelistedApps = whitelistedPackages.map { pkg -> resolveApp(pkg) }
                .sortedBy { it.label.lowercase() }

            _uiState.update {
                it.copy(
                    isEnabled = isEnabled,
                    whitelistedApps = whitelistedApps,
                    isLoading = false
                )
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        preferencesManager.isAppWhitelistEnabled = enabled
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    fun showPicker() {
        _uiState.update { it.copy(showPicker = true, searchQuery = "", isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val installed = loadInstalledApps()
            _uiState.update { it.copy(installedApps = installed, isLoading = false) }
        }
    }

    fun hidePicker() {
        _uiState.update { it.copy(showPicker = false, searchQuery = "") }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addApp(packageName: String) {
        preferencesManager.addAppToWhitelist(packageName)
        val app = resolveApp(packageName)
        _uiState.update { state ->
            state.copy(
                whitelistedApps = (state.whitelistedApps + app).sortedBy { it.label.lowercase() },
                installedApps = state.installedApps.filter { it.packageName != packageName },
                showPicker = false,
                searchQuery = ""
            )
        }
    }

    fun removeApp(packageName: String) {
        preferencesManager.removeAppFromWhitelist(packageName)
        _uiState.update { state ->
            state.copy(
                whitelistedApps = state.whitelistedApps.filter { it.packageName != packageName }
            )
        }
    }

    private fun resolveApp(packageName: String): InstalledApp {
        return try {
            val pm = application.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            InstalledApp(
                packageName = packageName,
                label = pm.getApplicationLabel(appInfo).toString(),
                icon = pm.getApplicationIcon(appInfo),
                isInstalled = true
            )
        } catch (_: Exception) {
            InstalledApp(
                packageName = packageName,
                label = packageName,
                icon = null,
                isInstalled = false
            )
        }
    }

    private fun loadInstalledApps(): List<InstalledApp> {
        val pm = application.packageManager
        val whitelisted = preferencesManager.appWhitelistPackages
        val ownPackage = application.packageName
        val excludedPackages = NotificationInterceptorService.MESSAGING_PACKAGES + ownPackage + whitelisted

        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        return resolveInfos
            .mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg in excludedPackages) return@mapNotNull null
                InstalledApp(
                    packageName = pkg,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm),
                    isInstalled = true
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun getFilteredInstalledApps(): List<InstalledApp> {
        val state = _uiState.value
        if (state.searchQuery.isBlank()) return state.installedApps
        val query = state.searchQuery.lowercase()
        return state.installedApps.filter {
            it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
        }
    }
}
