package com.example.petshop.data.database

import android.content.Context
import com.example.petshop.data.repository.AuthRepository
import com.example.petshop.data.repository.CartRepository
import com.example.petshop.data.repository.CategoryRepository
import com.example.petshop.data.repository.CustomerRepository
import com.example.petshop.data.repository.OrderRepository
import com.example.petshop.data.repository.ProductRepository
/**
 * Singleton object that provides access to the database and repositories.
 */
object DatabaseProvider {

    private var database: PetShopDatabase? = null
    private var customerRepository: CustomerRepository? = null
    private var authRepository: AuthRepository? = null
    private var categoryRepository: CategoryRepository? = null
    private var productRepository: ProductRepository? = null
    private var cartRepository: CartRepository? = null
    private var orderRepository: OrderRepository? = null

    fun initializeDatabase(context: Context) {
        if (database == null) {
            database = PetShopDatabase.getInstance(context)
        }
    }

    fun getCustomerRepository(context: Context): CustomerRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (customerRepository == null) {
            customerRepository = CustomerRepository(database!!.customerDao())
        }

        return customerRepository!!
    }

    fun getAuthRepository(context: Context): AuthRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (authRepository == null) {
            authRepository = AuthRepository(database!!.customerDao(), context)
        }

        return authRepository!!
    }

    fun getCategoryRepository(context: Context): CategoryRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (categoryRepository == null) {
            categoryRepository = CategoryRepository(database!!.categoryDao())
        }

        return categoryRepository!!
    }

    fun getProductRepository(context: Context): ProductRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (productRepository == null) {
            productRepository = ProductRepository(
                database!!.productDao(),
                database!!.categoryDao()
            )
        }

        return productRepository!!
    }

    fun getCartRepository(context: Context): CartRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (cartRepository == null) {
            cartRepository = CartRepository(database!!.cartItemDao())
        }

        return cartRepository!!
    }

    fun getOrderRepository(context: Context): OrderRepository {
        if (database == null) {
            initializeDatabase(context)
        }

        if (orderRepository == null) {
            orderRepository = OrderRepository(
                database!!.orderDao(),
                database!!.cartItemDao()
            )
        }

        return orderRepository!!
    }

}