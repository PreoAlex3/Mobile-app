package com.example.petshop.data.database
//last ver
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.petshop.data.repository.CartItemWithProduct
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Customer entity.
 * Defines methods for accessing and manipulating customer data in the database.
 */
@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Long): Customer?

    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1")
    suspend fun getCustomerByEmail(email: String): Customer?

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :searchQuery || '%' OR email LIKE '%' || :searchQuery || '%'")
    fun searchCustomers(searchQuery: String): Flow<List<Customer>>

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Long): Product?

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsByCategoryId(categoryId: Long): Flow<List<Product>>

    @Transaction
    @Query("SELECT p.* FROM products p INNER JOIN categories c ON p.categoryId = c.id WHERE c.name = :categoryName")
    fun getProductsByCategoryName(categoryName: String): Flow<List<Product>>

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM products WHERE categoryId = :categoryId")
    suspend fun deleteProductsByCategoryId(categoryId: Long)

    @Transaction
    @Query("SELECT * FROM products WHERE categoryId IN (SELECT id FROM categories WHERE name = :categoryName)")
    fun getProductsByCategory(categoryName: String): Flow<List<Product>>
}

@Dao
interface CartItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    fun getCartItemsByCustomerId(customerId: Long): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE id = :cartItemId")
    suspend fun getCartItemById(cartItemId: Long): CartItem?

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId AND productId = :productId")
    suspend fun getCartItemByCustomerAndProduct(customerId: Long, productId: Long): CartItem?

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCart(customerId: Long)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeProductFromCart(customerId: Long, productId: Long)

    @Query("SELECT COUNT(*) FROM cart_items WHERE customerId = :customerId")
    fun getCartItemCount(customerId: Long): Flow<Int>

    @Query("SELECT SUM(p.price * ci.quantity) FROM cart_items ci JOIN products p ON ci.productId = p.id WHERE ci.customerId = :customerId")
    fun getCartTotal(customerId: Long): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    fun getCartWithProducts(customerId: Long): Flow<List<CartItemWithProduct>>
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(orderItems: List<OrderItem>)

    @Transaction
    suspend fun insertOrderWithItems(order: Order, orderItems: List<OrderItem>): Long {
        val orderId = insertOrder(order)
        val updatedOrderItems = orderItems.map { it.copy(orderId = orderId) }
        insertOrderItems(updatedOrderItems)
        return orderId
    }

    data class OrderWithItems(
        @Embedded val order: Order,
        @Relation(
            entity = OrderItem::class,
            parentColumn = "id",
            entityColumn = "orderId"
        )
        val orderItems: List<OrderItemWithProduct>
    )

    // Relation class for joining OrderItem with Product
    data class OrderItemWithProduct(
        @Embedded val orderItem: OrderItem,
        @Relation(
            parentColumn = "productId",
            entityColumn = "id"
        )
        val product: Product
    )

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    fun getOrdersByCustomerId(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): Order?

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItems(orderId: Long): OrderWithItems?

    @Transaction
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    fun getCustomerOrdersWithItems(customerId: Long): Flow<List<OrderWithItems>>

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("SELECT COUNT(*) FROM orders WHERE customerId = :customerId")
    fun getCustomerOrderCount(customerId: Long): Flow<Int>

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<Order>>
}

