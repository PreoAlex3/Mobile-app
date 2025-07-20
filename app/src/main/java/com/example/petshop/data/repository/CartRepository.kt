package com.example.petshop.data.repository

import com.example.petshop.data.database.CartItem
import com.example.petshop.data.database.CartItemDao
import com.example.petshop.data.database.Product
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartItemDao: CartItemDao) {

    fun getCartWithProducts(customerId: Long): Flow<List<CartItemWithProduct>> =
        cartItemDao.getCartWithProducts(customerId)

    fun getCartItemCount(customerId: Long): Flow<Int> =
        cartItemDao.getCartItemCount(customerId)

    fun getCartTotal(customerId: Long): Flow<Double?> =
        cartItemDao.getCartTotal(customerId)

    suspend fun addToCart(customerId: Long, productId: Long, quantity: Int = 1): Long {
        val existingItem = cartItemDao.getCartItemByCustomerAndProduct(customerId, productId)

        return if (existingItem != null) {
            // Update quantity if already in cart
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
            cartItemDao.updateCartItem(updatedItem)
            existingItem.id
        } else {
            // Add new item to cart
            val cartItem = CartItem(
                customerId = customerId,
                productId = productId,
                quantity = quantity
            )
            cartItemDao.insertCartItem(cartItem)
        }
    }

    suspend fun updateQuantity(cartItemId: Long, quantity: Int) {
        val cartItem = cartItemDao.getCartItemById(cartItemId)
        if (cartItem != null && quantity > 0) {
            cartItemDao.updateCartItem(cartItem.copy(quantity = quantity))
        }
    }

    suspend fun removeFromCart(cartItem: CartItem) {
        cartItemDao.deleteCartItem(cartItem)
    }

    suspend fun clearCart(customerId: Long) {
        cartItemDao.clearCart(customerId)
    }
}