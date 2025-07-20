package com.example.petshop.data.repository


import com.example.petshop.data.database.Customer
import com.example.petshop.data.database.CustomerDao

/**
 * Repository class that abstracts the data operations on Customer entities.
 * Provides a clean API for the rest of the application to interact with the data layer.
 */
class CustomerRepository(private val customerDao: CustomerDao) {

    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)

}
