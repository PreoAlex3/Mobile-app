package com.example.petshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.OrderDao
import com.example.petshop.data.repository.AuthRepository
import com.example.petshop.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderDao.OrderWithItems>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadOrders() {
        val currentUserId = authRepository.getCurrentUserId() ?: return

        _isLoading.value = true
        viewModelScope.launch {
            orderRepository.getCustomerOrdersWithItems(currentUserId).collectLatest { ordersList ->
                _orders.value = ordersList
                _isLoading.value = false
            }
        }
    }
}

class OrdersViewModelFactory(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrdersViewModel(orderRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}