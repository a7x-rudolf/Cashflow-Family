package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val family: Family? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _updateNameState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updateNameState: StateFlow<Resource<Unit>> = _updateNameState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val changePasswordState: StateFlow<Resource<Unit>> = _changePasswordState.asStateFlow()

    private val _updatePhotoState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updatePhotoState: StateFlow<Resource<Unit>> = _updatePhotoState.asStateFlow()

    // Flag untuk trigger logout setelah ganti password
    private val _passwordChangedSuccessfully = MutableStateFlow(false)
    val passwordChangedSuccessfully: StateFlow<Boolean> = _passwordChangedSuccessfully.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        val currentAuthUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentAuthUser.uid)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(user = user)

                    if (user.familyId.isNotEmpty()) {
                        familyRepository.getFamilyById(user.familyId)
                            .onSuccess { family ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    family = family
                                )
                            }
                            .onFailure {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
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

    fun updateName(newName: String) {
        val user = _uiState.value.user ?: return

        viewModelScope.launch {
            _updateNameState.value = Resource.Loading

            authRepository.updateUserName(user.userId, newName)
                .onSuccess {
                    _updateNameState.value = Resource.Success(Unit)
                    // Update local state
                    _uiState.value = _uiState.value.copy(
                        user = user.copy(name = newName)
                    )
                }
                .onFailure { error ->
                    _updateNameState.value = Resource.Error(
                        error.message ?: "Gagal memperbarui nama"
                    )
                }
        }
    }

    fun updatePhoto(photoDataUri: String) {
        val user = _uiState.value.user ?: return

        viewModelScope.launch {
            _updatePhotoState.value = Resource.Loading

            authRepository.updateUserPhoto(user.userId, photoDataUri)
                .onSuccess {
                    _updatePhotoState.value = Resource.Success(Unit)
                    _uiState.value = _uiState.value.copy(
                        user = user.copy(photoUrl = photoDataUri)
                    )
                }
                .onFailure { error ->
                    _updatePhotoState.value = Resource.Error(
                        error.message ?: "Gagal memperbarui foto profil"
                    )
                }
        }
    }

    fun removePhoto() {
        val user = _uiState.value.user ?: return

        viewModelScope.launch {
            _updatePhotoState.value = Resource.Loading

            authRepository.removeUserPhoto(user.userId)
                .onSuccess {
                    _updatePhotoState.value = Resource.Success(Unit)
                    _uiState.value = _uiState.value.copy(
                        user = user.copy(photoUrl = "")
                    )
                }
                .onFailure { error ->
                    _updatePhotoState.value = Resource.Error(
                        error.message ?: "Gagal menghapus foto profil"
                    )
                }
        }
    }

    fun resetUpdatePhotoState() {
        _updatePhotoState.value = Resource.Idle
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading

            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _changePasswordState.value = Resource.Success(Unit)
                    _passwordChangedSuccessfully.value = true  // <-- Set flag
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: ""

                    val friendlyMessage = when {
                        errorMessage.contains("credential is incorrect", ignoreCase = true) ||
                                errorMessage.contains("credential is malformed", ignoreCase = true) ||
                                errorMessage.contains("password is invalid", ignoreCase = true) ||
                                errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                                errorMessage.contains("wrong password", ignoreCase = true) ||
                                errorMessage.contains("has expired", ignoreCase = true) ->
                            "Password lama yang Anda masukkan salah"

                        errorMessage.contains("weak-password", ignoreCase = true) ||
                                errorMessage.contains("weak password", ignoreCase = true) ->
                            "Password baru terlalu lemah, gunakan password yang lebih kuat"

                        errorMessage.contains("too-many-requests", ignoreCase = true) ||
                                errorMessage.contains("too many", ignoreCase = true) ->
                            "Terlalu banyak percobaan, silakan coba lagi nanti"

                        errorMessage.contains("requires-recent-login", ignoreCase = true) ->
                            "Silakan logout dan login ulang, kemudian coba lagi"

                        errorMessage.contains("network", ignoreCase = true) ->
                            "Koneksi bermasalah, periksa internet Anda"

                        else -> "Gagal mengubah password. Silakan coba lagi"
                    }

                    _changePasswordState.value = Resource.Error(friendlyMessage)
                }
        }
    }

    fun resetUpdateNameState() {
        _updateNameState.value = Resource.Idle
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = Resource.Idle
    }

    fun resetPasswordChangedFlag() {
        _passwordChangedSuccessfully.value = false
    }
}