package com.app.cashflowfamily.utils

object PasswordValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    // Aturan password
    private const val MIN_LENGTH = 8

    fun validate(password: String): ValidationResult {
        return when {
            password.isBlank() -> {
                ValidationResult(false, "Password wajib diisi")
            }
            password.length < MIN_LENGTH -> {
                ValidationResult(false, "Password minimal $MIN_LENGTH karakter")
            }
            !password.any { it.isUpperCase() } -> {
                ValidationResult(false, "Password harus mengandung huruf besar (A-Z)")
            }
            !password.any { it.isLowerCase() } -> {
                ValidationResult(false, "Password harus mengandung huruf kecil (a-z)")
            }
            !password.any { it.isDigit() } -> {
                ValidationResult(false, "Password harus mengandung angka (0-9)")
            }
            else -> {
                ValidationResult(true)
            }
        }
    }

}

