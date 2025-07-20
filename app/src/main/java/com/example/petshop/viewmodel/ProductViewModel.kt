package com.example.petshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.Product
import com.example.petshop.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    fun loadProductsByCategory(categoryId: Long) {
        viewModelScope.launch {
            productRepository.getProductsByCategoryId(categoryId).collectLatest { productList ->
                _products.value = productList
            }
        }
    }

    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> {
        return productRepository.getProductsByCategoryId(categoryId)
    }

    suspend fun getProductById(productId: Long): Product? {
        return productRepository.getProductById(productId)
    }
}

class ProductViewModelFactory(private val productRepository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}