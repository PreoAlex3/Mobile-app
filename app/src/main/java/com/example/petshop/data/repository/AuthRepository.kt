package com.example.petshop.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.petshop.data.database.Customer
import com.example.petshop.data.database.CustomerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Repository for handling authentication-related operations.
 */
class AuthRepository(
    private val customerDao: CustomerDao,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("petshop_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean {
        return prefs.getLong(KEY_LOGGED_IN_USER_ID, -1) != -1L
    }

    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_LOGGED_IN_USER_ID, -1)
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Check if email already exists
            val existingUser = customerDao.getCustomerByEmail(email)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("Email already exists"))
            }

            // Hash the password
            val hashedPassword = hashPassword(password)

            // Create customer object
            val customer = Customer(
                name = name,
                email = email,
                phone = phone,
                address = address,
                password = hashedPassword
            )

            // Insert customer and get ID
            val customerId = customerDao.insertCustomer(customer)

            // Log the user in
            prefs.edit().putLong(KEY_LOGGED_IN_USER_ID, customerId).apply()

            Result.success(customerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Customer> = withContext(Dispatchers.IO) {
        try {
            // Get customer by email
            val customer = customerDao.getCustomerByEmail(email)
                ?: return@withContext Result.failure(Exception("Email not found"))

            // Check password
            val hashedPassword = hashPassword(password)
            if (customer.password != hashedPassword) {
                return@withContext Result.failure(Exception("Invalid password"))
            }

            // Set logged-in user
            prefs.edit().putLong(KEY_LOGGED_IN_USER_ID, customer.id).apply()

            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER_ID).apply()
    }

    suspend fun getCurrentUser(): Customer? = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        if (userId == -1L) return@withContext null

        customerDao.getCustomerById(userId)
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            if (userId == -1L) {
                return@withContext Result.failure(Exception("Not logged in"))
            }

            val customer = customerDao.getCustomerById(userId)
                ?: return@withContext Result.failure(Exception("User not found"))

            // Verify current password
            val hashedCurrentPassword = hashPassword(currentPassword)
            if (customer.password != hashedCurrentPassword) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            // Update password
            val hashedNewPassword = hashPassword(newPassword)
            val updatedCustomer = customer.copy(password = hashedNewPassword)
            customerDao.updateCustomer(updatedCustomer)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            if (userId == -1L) {
                return@withContext Result.failure(Exception("Not logged in"))
            }

            val customer = customerDao.getCustomerById(userId)
                ?: return@withContext Result.failure(Exception("User not found"))

            // Verify password
            val hashedPassword = hashPassword(password)
            if (customer.password != hashedPassword) {
                return@withContext Result.failure(Exception("Password is incorrect"))
            }

            // Delete customer
            customerDao.deleteCustomer(customer)

            // Log out
            logout()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
    }
}