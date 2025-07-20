package com.example.petshop.data.repository

import com.example.petshop.data.database.Category
import com.example.petshop.data.database.CategoryDao

import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

}