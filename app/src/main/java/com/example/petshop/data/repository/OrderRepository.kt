package com.example.petshop.data.repository

import com.example.petshop.data.database.*
import com.example.petshop.data.database.OrderDao.OrderWithItems
import kotlinx.coroutines.flow.Flow

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartItemDao: CartItemDao
) {

    fun getCustomerOrdersWithItems(customerId: Long): Flow<List<OrderWithItems>> =
        orderDao.getCustomerOrdersWithItems(customerId)

    suspend fun getOrderWithItems(orderId: Long): OrderWithItems? =
        orderDao.getOrderWithItems(orderId)

    suspend fun createOrderFromCart(
        customerId: Long,
        shippingAddress: String,
        paymentMethod: String,
        notes: String? = null,
        cartItems: List<CartItemWithProduct>
    ): Result<Long> {
        return try {
            // Calculate total amount
            val totalAmount = cartItems.sumOf { it.cartItem.quantity * it.product.price }

            // Create order
            val order = Order(
                customerId = customerId,
                totalAmount = totalAmount,
                shippingAddress = shippingAddress,
                paymentMethod = paymentMethod,
                notes = notes
            )

            // Create order items from cart items
            val orderItems = cartItems.map { cartItemWithProduct ->
                val cartItem = cartItemWithProduct.cartItem
                val product = cartItemWithProduct.product

                OrderItem(
                    orderId = 0, // Will be updated after order insertion
                    productId = product.id,
                    quantity = cartItem.quantity,
                    unitPrice = product.price,
                    totalPrice = product.price * cartItem.quantity
                )
            }

            // Insert order and order items in a transaction
            val orderId = orderDao.insertOrderWithItems(order, orderItems)

            // Clear the cart after successful order creation
            cartItemDao.clearCart(customerId)

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}