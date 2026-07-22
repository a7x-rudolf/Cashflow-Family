package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberInfo(
    val user: User,
    val transactionCount: Int = 0,
    val isCurrentUser: Boolean = false
)

data class FamilyManagementUiState(
    val isLoading: Boolean = false,
    val family: Family? = null,
    val currentUser: User? = null,
    val members: List<MemberInfo> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class FamilyManagementViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyManagementUiState())
    val uiState: StateFlow<FamilyManagementUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val actionState: StateFlow<Resource<String>> = _actionState.asStateFlow()

    init {
        loadFamilyData()
    }

    fun loadFamilyData() {
        val currentAuthUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentAuthUser.uid)
                .onSuccess { user ->
                    if (user.familyId.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Anda belum terdaftar di keluarga"
                        )
                        return@onSuccess
                    }

                    _uiState.value = _uiState.value.copy(currentUser = user)

                    // Load family
                    familyRepository.getFamilyById(user.familyId)
                        .onSuccess { family ->
                            _uiState.value = _uiState.value.copy(family = family)

                            // Load members
                            loadMembers(family, user)
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

    private fun loadMembers(family: Family, currentUser: User) {
        viewModelScope.launch {
            // Load members data
            familyRepository.getFamilyMembers(family.members)
                .onSuccess { users ->
                    // Load transaction counts bulan ini
                    val now = System.currentTimeMillis()
                    val startOfMonth = DateFormatter.getStartOfMonth(now)
                    val endOfMonth = DateFormatter.getEndOfMonth(now)

                    transactionRepository.countTransactionsByUser(
                        familyId = family.familyId,
                        startOfMonth = startOfMonth,
                        endOfMonth = endOfMonth
                    ).onSuccess { countMap ->
                        val memberInfos = users.map { user ->
                            MemberInfo(
                                user = user,
                                transactionCount = countMap[user.userId] ?: 0,
                                isCurrentUser = user.userId == currentUser.userId
                            )
                        }.sortedWith(
                            compareByDescending<MemberInfo> { it.isCurrentUser }
                                .thenByDescending { it.user.role == "admin" }
                                .thenBy { it.user.name }
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            members = memberInfos
                        )
                    }.onFailure {
                        // Kalau count gagal, tetap tampilkan member tanpa count
                        val memberInfos = users.map { user ->
                            MemberInfo(
                                user = user,
                                transactionCount = 0,
                                isCurrentUser = user.userId == currentUser.userId
                            )
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            members = memberInfos
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

    // Keluar dari keluarga
    fun leaveFamily() {
        val family = _uiState.value.family ?: return
        val user = _uiState.value.currentUser ?: return

        viewModelScope.launch {
            _actionState.value = Resource.Loading

            familyRepository.leaveFamily(family.familyId, user.userId)
                .onSuccess {
                    _actionState.value = Resource.Success("Anda berhasil keluar dari keluarga")
                    _hasLeftFamily.value = true  // Set flag
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal keluar dari keluarga"
                    )
                }
        }
    }

    // Kick member (admin only)
    fun kickMember(memberUserId: String, memberName: String) {
        val family = _uiState.value.family ?: return

        viewModelScope.launch {
            _actionState.value = Resource.Loading

            familyRepository.kickMember(family.familyId, memberUserId)
                .onSuccess {
                    _actionState.value = Resource.Success("$memberName berhasil dikeluarkan")
                    loadFamilyData() // Refresh
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal mengeluarkan anggota"
                    )
                }
        }
    }

    // Promote member jadi admin
    fun promoteToAdmin(memberUserId: String, memberName: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading

            familyRepository.promoteToAdmin(memberUserId)
                .onSuccess {
                    _actionState.value = Resource.Success("$memberName sekarang menjadi Admin")
                    loadFamilyData() // Refresh
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal menjadikan admin"
                    )
                }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Idle
    }

    private val _hasLeftFamily = MutableStateFlow(false)
    val hasLeftFamily: StateFlow<Boolean> = _hasLeftFamily.asStateFlow()
}