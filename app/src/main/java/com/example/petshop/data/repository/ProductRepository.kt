package com.example.petshop.data.repository

import com.example.petshop.data.database.Category
import com.example.petshop.data.database.CategoryDao
import com.example.petshop.data.database.Product
import com.example.petshop.data.database.ProductDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Product operations
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) {
    fun getProductsByCategoryId(categoryId: Long): Flow<List<Product>> =
        productDao.getProductsByCategoryId(categoryId)

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

}