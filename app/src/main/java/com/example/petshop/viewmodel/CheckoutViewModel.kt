package com.example.petshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.repository.AuthRepository
import com.example.petshop.data.repository.CartItemWithProduct
import com.example.petshop.data.repository.CartRepository
import com.example.petshop.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItemWithProduct>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal = _cartTotal.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    fun loadCartItems() {
        val currentUserId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            cartRepository.getCartWithProducts(currentUserId).collectLatest { items ->
                _cartItems.value = items
            }
        }

        viewModelScope.launch {
            cartRepository.getCartTotal(currentUserId).collectLatest { total ->
                _cartTotal.value = total ?: 0.0
            }
        }
    }

    suspend fun placeOrder(
        shippingAddress: String,
        paymentMethod: String,
        notes: String? = null
    ): Result<Long> {
        val currentUserId = authRepository.getCurrentUserId()

        if (_cartItems.value.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }

        _isProcessing.value = true

        try {
            val result = orderRepository.createOrderFromCart(
                customerId = currentUserId,
                shippingAddress = shippingAddress,
                paymentMethod = paymentMethod,
                notes = notes,
                cartItems = _cartItems.value
            )

            if (result.isSuccess) {
                _cartItems.value = emptyList()
                _cartTotal.value = 0.0
            }

            return result
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            _isProcessing.value = false
        }
    }
}

class CheckoutViewModelFactory(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(cartRepository, orderRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}