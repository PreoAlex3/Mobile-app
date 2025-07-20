package com.example.petshop.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.OrderDao
import com.example.petshop.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        viewModelScope.launch {
            try {
                val orderWithItems = orderRepository.getOrderWithItems(orderId)
                if (orderWithItems != null) {
                    _uiState.value = OrderDetailUiState.Success(orderWithItems)
                } else {
                    _uiState.value = OrderDetailUiState.Error("Order not found")
                }
            } catch (e: Exception) {
                _uiState.value = OrderDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

}

sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val orderWithItems: OrderDao.OrderWithItems) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

class OrderDetailViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            val savedStateHandle = SavedStateHandle().apply {
                set("orderId", orderId)
            }
            return OrderDetailViewModel(orderRepository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}