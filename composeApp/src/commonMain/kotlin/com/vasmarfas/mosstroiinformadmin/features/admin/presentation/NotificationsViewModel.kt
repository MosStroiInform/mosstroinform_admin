package com.vasmarfas.mosstroiinformadmin.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.NotificationResponse
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationResponse> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

class NotificationsViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(unreadOnly: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getNotifications(unreadOnly)) {
                is ApiResult.Success -> {
                    val unreadCount = result.data.count { !it.isRead }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        notifications = result.data,
                        unreadCount = unreadCount
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
            // Обновляем локальное состояние
            _state.value = _state.value.copy(
                notifications = _state.value.notifications.map { notif ->
                    if (notif.id == notificationId) {
                        notif.copy(isRead = true)
                    } else {
                        notif
                    }
                },
                unreadCount = maxOf(0, _state.value.unreadCount - 1)
            )
        }
    }

    fun refreshNotifications() {
        loadNotifications()
    }
}

