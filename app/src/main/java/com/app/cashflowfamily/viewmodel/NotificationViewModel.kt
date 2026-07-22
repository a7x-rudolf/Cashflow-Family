package com.app.cashflowfamily.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Notification
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.NotificationRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Listener registrations untuk clean up
    private var notificationListener: ListenerRegistration? = null
    private var unreadCountListener: ListenerRegistration? = null

    // Flag untuk tracking apakah listener sudah di-start
    private var isListenerActive = false

    override fun onCleared() {
        super.onCleared()
        stopRealTimeListener()
    }

    // ===== LOAD DATA (SUSPEND) =====

    fun loadData() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    notificationRepository.getUnreadCount(user.userId)
                        .onSuccess { count ->
                            _uiState.value = _uiState.value.copy(unreadCount = count)
                        }

                    notificationRepository.getNotifications(user.userId)
                        .onSuccess { notifications ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                notifications = notifications
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    // ===== REAL-TIME LISTENER =====

    fun startRealTimeListener() {
        if (isListenerActive) {
            Log.d("NotificationVM", "Listener already active")
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.e("NotificationVM", "Cannot start listener: user not logged in")
            return
        }

        viewModelScope.launch {
            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    // Remove existing listeners
                    stopRealTimeListener()

                    Log.d("NotificationVM", "Starting real-time listeners for user: ${user.userId}")

                    // 1. Listener untuk daftar notifikasi
                    notificationListener = notificationRepository.observeNotifications(
                        userId = user.userId,
                        onUpdate = { notifications ->
                            Log.d("NotificationVM", "Notifications updated: ${notifications.size}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                notifications = notifications
                            )
                        },
                        onError = { error ->
                            Log.e("NotificationVM", "Notification listener error", error)
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message
                            )
                        }
                    )

                    // 2. Listener untuk unread count (badge)
                    unreadCountListener = notificationRepository.observeUnreadCount(
                        userId = user.userId,
                        onUpdate = { count ->
                            Log.d("NotificationVM", "Unread count updated: $count")
                            _uiState.value = _uiState.value.copy(unreadCount = count)
                        },
                        onError = { error ->
                            Log.e("NotificationVM", "Unread count listener error", error)
                        }
                    )

                    isListenerActive = true
                }
                .onFailure { error ->
                    Log.e("NotificationVM", "Failed to get user data for listener", error)
                }
        }
    }

    fun stopRealTimeListener() {
        Log.d("NotificationVM", "Stopping real-time listeners")
        notificationListener?.remove()
        notificationListener = null
        unreadCountListener?.remove()
        unreadCountListener = null
        isListenerActive = false
    }

    // ===== ACTIONS =====

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess {
                    // Update local state (listener juga akan update, tapi ini untuk immediate feedback)
                    val updated = _uiState.value.notifications.map { notification ->
                        if (notification.notificationId == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        notifications = updated,
                        unreadCount = (_uiState.value.unreadCount - 1).coerceAtLeast(0)
                    )
                }
                .onFailure { error ->
                    Log.e("NotificationVM", "Failed to mark as read", error)
                }
        }
    }

    fun markAllAsRead() {
        val currentUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    notificationRepository.markAllAsRead(user.userId)
                        .onSuccess {
                            val updated = _uiState.value.notifications.map { it.copy(isRead = true) }
                            _uiState.value = _uiState.value.copy(
                                notifications = updated,
                                unreadCount = 0
                            )
                        }
                        .onFailure { error ->
                            Log.e("NotificationVM", "Failed to mark all as read", error)
                        }
                }
                .onFailure { error ->
                    Log.e("NotificationVM", "Failed to get user data", error)
                }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
                .onSuccess {
                    val updated = _uiState.value.notifications.filter {
                        it.notificationId != notificationId
                    }
                    _uiState.value = _uiState.value.copy(notifications = updated)
                }
                .onFailure { error ->
                    Log.e("NotificationVM", "Failed to delete notification", error)
                }
        }
    }
}