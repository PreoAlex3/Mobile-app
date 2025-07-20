package com.example.petshop.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.Customer
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.repository.AuthRepository
import com.example.petshop.data.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ContentResolver
import java.io.File
import java.io.FileOutputStream

class AccountViewModel(
    private val customerRepository: CustomerRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadCustomerData()
    }

    private fun loadCustomerData() {
        viewModelScope.launch {
            val customer = authRepository.getCurrentUser()
            if (customer != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        customer = customer,
                        nameInput = customer.name,
                        emailInput = customer.email,
                        phoneInput = customer.phone,
                        addressInput = customer.address,
                        profileImagePathInput = customer.profileImagePath ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User not found") }
            }
        }
    }

    fun setEditMode(editing: Boolean) {
        _uiState.update {
            it.copy(
                isEditing = editing,
                // Reset form values if canceling edit
                nameInput = if (!editing) it.customer?.name ?: "" else it.nameInput,
                emailInput = if (!editing) it.customer?.email ?: "" else it.emailInput,
                phoneInput = if (!editing) it.customer?.phone ?: "" else it.phoneInput,
                addressInput = if (!editing) it.customer?.address ?: "" else it.addressInput,
                profileImagePathInput = if (!editing) it.customer?.profileImagePath ?: "" else it.profileImagePathInput
            )
        }
    }

    fun updateNameInput(name: String) {
        _uiState.update { it.copy(nameInput = name) }
    }

    fun updateEmailInput(email: String) {
        _uiState.update { it.copy(emailInput = email) }
    }

    fun updatePhoneInput(phone: String) {
        _uiState.update { it.copy(phoneInput = phone) }
    }

    fun updateAddressInput(address: String) {
        _uiState.update { it.copy(addressInput = address) }
    }

    fun handleImageSelection(uri: Uri) {
        viewModelScope.launch {
            try {
                val permanentPath = savePermanentImageCopy(context, uri)
                _uiState.update { it.copy(profileImagePathInput = permanentPath) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to save image: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentCustomer = _uiState.value.customer
                if (currentCustomer != null) {
                    val updatedCustomer = currentCustomer.copy(
                        name = _uiState.value.nameInput,
                        email = _uiState.value.emailInput,
                        phone = _uiState.value.phoneInput,
                        address = _uiState.value.addressInput,
                        profileImagePath = _uiState.value.profileImagePathInput
                    )

                    customerRepository.updateCustomer(updatedCustomer)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            customer = updatedCustomer,
                            isEditing = false,
                            successMessage = "Profile updated successfully!"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to update profile: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private suspend fun savePermanentImageCopy(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        val imagesDir = File(context.filesDir, "profile_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val fileName = getFileName(contentResolver, uri) ?: "profile_${System.currentTimeMillis()}.jpg"

        val destFile = File(imagesDir, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        destFile.absolutePath
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            result?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) {
                    result = it.substring(cut + 1)
                }
            }
        }
        return result
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                val customerRepository = DatabaseProvider.getCustomerRepository(context)
                val authRepository = DatabaseProvider.getAuthRepository(context)
                return AccountViewModel(customerRepository, authRepository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

data class AccountUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val customer: Customer? = null,
    val nameInput: String = "",
    val emailInput: String = "",
    val phoneInput: String = "",
    val addressInput: String = "",
    val profileImagePathInput: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)