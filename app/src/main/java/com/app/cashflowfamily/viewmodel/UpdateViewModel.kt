package com.app.cashflowfamily.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.utils.UpdateChecker
import com.app.cashflowfamily.utils.UpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val updateChecker = UpdateChecker(application)

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            _isChecking.value = true
            _error.value = null

            try {
                val result = updateChecker.checkForUpdate()
                _updateInfo.value = result

                if (result == null) {
                    val context = getApplication<Application>().applicationContext
                    Toast.makeText(
                        context,
                        "Tidak ada update tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                _error.value = "Gagal mengecek update: ${e.message}"
                val context = getApplication<Application>().applicationContext
                Toast.makeText(
                    context,
                    "Gagal mengecek update. Coba lagi.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun clearUpdateInfo() {
        _updateInfo.value = null
        _error.value = null
    }
}