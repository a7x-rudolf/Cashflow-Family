package com.app.cashflowfamily.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.BackupRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.utils.backup.BackupHelper
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val family: Family? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backupState = MutableStateFlow<Resource<File>>(Resource.Idle)
    val backupState: StateFlow<Resource<File>> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow<Resource<RestoreResult>>(Resource.Idle)
    val restoreState: StateFlow<Resource<RestoreResult>> = _restoreState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val currentAuthUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentAuthUser.uid)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(currentUser = user)

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

    fun createBackup() {
        val family = _uiState.value.family ?: return

        viewModelScope.launch {
            _backupState.value = Resource.Loading

            backupRepository.gatherBackupData(family)
                .onSuccess { backupData ->
                    withContext(Dispatchers.IO) {
                        val file = BackupHelper.exportBackup(context, backupData)
                        if (file != null) {
                            _backupState.value = Resource.Success(file)
                        } else {
                            _backupState.value = Resource.Error("Gagal membuat file backup")
                        }
                    }
                }
                .onFailure { error ->
                    _backupState.value = Resource.Error(
                        error.message ?: "Gagal mengumpulkan data backup"
                    )
                }
        }
    }

    fun restoreBackup(fileUri: Uri) {
        val user = _uiState.value.currentUser ?: return
        val family = _uiState.value.family ?: return

        viewModelScope.launch {
            _restoreState.value = Resource.Loading

            // Parse file
            val backupData = withContext(Dispatchers.IO) {
                BackupHelper.importBackup(context, fileUri)
            }

            if (backupData == null) {
                _restoreState.value = Resource.Error("File backup tidak valid atau rusak")
                return@launch
            }

            // Validasi
            val validation = BackupHelper.validateBackup(backupData)
            if (!validation.isValid) {
                _restoreState.value = Resource.Error(validation.message)
                return@launch
            }

            // Restore
            backupRepository.restoreBackup(backupData, user, family)
                .onSuccess { result ->
                    _restoreState.value = Resource.Success(result)
                }
                .onFailure { error ->
                    _restoreState.value = Resource.Error(
                        error.message ?: "Gagal restore data"
                    )
                }
        }
    }

    fun resetBackupState() {
        _backupState.value = Resource.Idle
    }

    fun resetRestoreState() {
        _restoreState.value = Resource.Idle
    }
}