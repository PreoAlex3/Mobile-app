package com.example.petshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.CartItem
import com.example.petshop.data.repository.CartItemWithProduct
import com.example.petshop.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItemWithProduct>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal = _cartTotal.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount = _cartItemCount.asStateFlow()

    fun loadCartItems(customerId: Long) {
        viewModelScope.launch {
            cartRepository.getCartWithProducts(customerId).collectLatest { items ->
                _cartItems.value = items
            }
        }

        viewModelScope.launch {
            cartRepository.getCartTotal(customerId).collectLatest { total ->
                _cartTotal.value = total ?: 0.0
            }
        }

        viewModelScope.launch {
            cartRepository.getCartItemCount(customerId).collectLatest { count ->
                _cartItemCount.value = count
            }
        }
    }

    suspend fun addToCart(customerId: Long, productId: Long, quantity: Int = 1) {
        cartRepository.addToCart(customerId, productId, quantity)
    }

    suspend fun updateQuantity(cartItemId: Long, quantity: Int) {
        cartRepository.updateQuantity(cartItemId, quantity)
    }

    suspend fun removeFromCart(cartItem: CartItem) {
        cartRepository.removeFromCart(cartItem)
    }

    fun clearCartDisplay() {
        _cartItems.value = emptyList()
        _cartTotal.value = 0.0
        _cartItemCount.value = 0
    }
}

class CartViewModelFactory(private val cartRepository: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(cartRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}