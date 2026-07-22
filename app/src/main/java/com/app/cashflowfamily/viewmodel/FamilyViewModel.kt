package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _familyState = MutableStateFlow<Resource<Family>>(Resource.Idle)
    val familyState: StateFlow<Resource<Family>> = _familyState.asStateFlow()

    // Cek apakah user sudah punya family
    fun checkUserHasFamily(onResult: (Boolean) -> Unit) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    onResult(user.familyId.isNotEmpty())
                }
                .onFailure {
                    onResult(false)
                }
        }
    }

    // Buat family baru
    fun createFamily(familyName: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _familyState.value = Resource.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _familyState.value = Resource.Loading

            familyRepository.createFamily(familyName, currentUser.uid)
                .onSuccess { family ->
                    _familyState.value = Resource.Success(family)
                }
                .onFailure { error ->
                    _familyState.value = Resource.Error(
                        error.message ?: "Gagal membuat keluarga"
                    )
                }
        }
    }

    // Gabung family
    fun joinFamily(familyCode: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _familyState.value = Resource.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _familyState.value = Resource.Loading

            familyRepository.joinFamily(familyCode, currentUser.uid)
                .onSuccess { family ->
                    _familyState.value = Resource.Success(family)
                }
                .onFailure { error ->
                    _familyState.value = Resource.Error(
                        error.message ?: "Gagal bergabung dengan keluarga"
                    )
                }
        }
    }

    fun resetState() {
        _familyState.value = Resource.Idle
    }
}