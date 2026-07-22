package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Feedback
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FeedbackRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val feedbacks: List<Feedback> = emptyList(),
    val isSending: Boolean = false,
    val sendSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val userEmail: String = ""
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private val _sendState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val sendState: StateFlow<Resource<String>> = _sendState.asStateFlow()

    fun loadFeedbacks() {
        val currentUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    feedbackRepository.getFeedbacks(user.userId)
                        .onSuccess { feedbacks ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                feedbacks = feedbacks,
                                userName = user.name,
                                userEmail = user.email
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

    fun sendFeedback(
        type: String,
        title: String,
        message: String
    ) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _sendState.value = Resource.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _sendState.value = Resource.Loading
            _uiState.value = _uiState.value.copy(isSending = true)

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    val feedback = Feedback(
                        userId = user.userId,
                        userName = user.name,
                        userEmail = user.email,
                        type = type,
                        title = title,
                        message = message,
                        createdAt = System.currentTimeMillis()
                    )

                    feedbackRepository.sendFeedback(feedback)
                        .onSuccess { _ ->  // Ganti savedFeedback dengan _
                            _sendState.value = Resource.Success("Feedback berhasil dikirim!")
                            _uiState.value = _uiState.value.copy(
                                isSending = false,
                                sendSuccess = true
                            )
                            loadFeedbacks()
                        }
                        .onFailure { error ->
                            _sendState.value = Resource.Error(
                                error.message ?: "Gagal mengirim feedback"
                            )
                            _uiState.value = _uiState.value.copy(isSending = false)
                        }
                }
                .onFailure { error ->
                    _sendState.value = Resource.Error(
                        error.message ?: "Gagal mengambil data user"
                    )
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
        }
    }

    fun resetSendState() {
        _sendState.value = Resource.Idle
        _uiState.value = _uiState.value.copy(sendSuccess = false)
    }
}