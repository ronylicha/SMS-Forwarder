package com.qrcommunication.smsforwarder.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcommunication.smsforwarder.data.local.entity.AppNotification
import com.qrcommunication.smsforwarder.data.repository.AppNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationCenterUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
)

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val repository: AppNotificationRepository,
) : ViewModel() {

    val uiState: StateFlow<NotificationCenterUiState> = kotlinx.coroutines.flow
        .combine(repository.observeAll(), repository.observeUnreadCount()) { all, unread ->
            NotificationCenterUiState(notifications = all, unreadCount = unread)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationCenterUiState(),
        )

    fun markAllAsRead() = viewModelScope.launch { repository.markAllAsRead() }

    fun markAsRead(id: Long) = viewModelScope.launch { repository.markAsRead(id) }

    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }

    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }
}
