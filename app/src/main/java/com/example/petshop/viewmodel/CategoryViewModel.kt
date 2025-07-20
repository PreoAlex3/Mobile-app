package com.example.petshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petshop.data.database.Category
import com.example.petshop.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    val categories: Flow<List<Category>> = categoryRepository.getAllCategories()
}

class CategoryViewModelFactory(private val categoryRepository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}