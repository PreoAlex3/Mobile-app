package com.example.petshop.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Update form field values
    fun updateName(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = if (it.nameError != null) validateName(value) else null
            )
        }
    }

    fun updateEmail(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = if (it.emailError != null) validateEmail(value) else null
            )
        }
    }

    fun updatePhone(value: String) {
        _uiState.update {
            it.copy(
                phone = value,
                phoneError = if (it.phoneError != null) validatePhone(value) else null
            )
        }
    }

    fun updateAddress(value: String) {
        _uiState.update {
            it.copy(
                address = value,
                addressError = if (it.addressError != null) validateAddress(value) else null
            )
        }
    }

    fun updatePassword(value: String) {
        _uiState.update {
            val newState = it.copy(
                password = value,
                passwordError = if (it.passwordError != null) validatePassword(value) else null
            )

            // If confirm password was already entered, validate it against the new password
            if (newState.confirmPassword.isNotEmpty()) {
                newState.copy(
                    confirmPasswordError = validateConfirmPassword(newState.confirmPassword, value)
                )
            } else newState
        }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = if (it.confirmPasswordError != null)
                    validateConfirmPassword(value, it.password) else null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    // Validation functions
    private fun validateName(value: String): String? {
        return if (value.length < 10) {
            "Full name must be at least 10 characters"
        } else {
            null
        }
    }

    private fun validateEmail(value: String): String? {
        return if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            "Please enter a valid email address"
        } else {
            null
        }
    }

    private fun validatePhone(value: String): String? {
        return if (value.length < 10) {
            "Phone number must be at least 10 characters"
        } else {
            null
        }
    }

    private fun validateAddress(value: String): String? {
        return if (value.length < 5) {
            "Address must be at least 5 characters"
        } else {
            null
        }
    }

    private fun validatePassword(value: String): String? {
        val hasUppercase = value.any { it.isUpperCase() }
        val hasLowercase = value.any { it.isLowerCase() }

        return when {
            value.length < 5 -> {
                "Password must be at least 5 characters"
            }
            !hasUppercase -> {
                "Password must contain at least one uppercase letter"
            }
            !hasLowercase -> {
                "Password must contain at least one lowercase letter"
            }
            else -> {
                null
            }
        }
    }

    private fun validateConfirmPassword(value: String, password: String): String? {
        return if (value != password) {
            "Passwords don't match"
        } else {
            null
        }
    }

    fun validateForm(): Boolean {
        // Run all validations
        val nameError = validateName(_uiState.value.name)
        val emailError = validateEmail(_uiState.value.email)
        val phoneError = validatePhone(_uiState.value.phone)
        val addressError = validateAddress(_uiState.value.address)
        val passwordError = validatePassword(_uiState.value.password)
        val confirmPasswordError = validateConfirmPassword(
            _uiState.value.confirmPassword,
            _uiState.value.password
        )

        // Update UI state with validation results
        _uiState.update {
            it.copy(
                nameError = nameError,
                emailError = emailError,
                phoneError = phoneError,
                addressError = addressError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        // Return true if all validations pass
        return nameError == null && emailError == null && phoneError == null &&
                addressError == null && passwordError == null && confirmPasswordError == null
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (validateForm()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isRegistering = true) }

                authRepository.register(
                    _uiState.value.name,
                    _uiState.value.email,
                    _uiState.value.phone,
                    _uiState.value.address,
                    _uiState.value.password
                )
                    .onSuccess {
                        _uiState.update { it.copy(isRegistering = false) }
                        onSuccess()
                    }
                    .onFailure {
                        _uiState.update { it.copy(isRegistering = false) }
                        onError(it.message ?: "Registration failed")
                    }
            }
        } else {
            onError("Please correct the errors in the form")
        }
    }

    // Factory class to create the ViewModel with dependencies
    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI State class to hold all the state for the Register screen
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isRegistering: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)