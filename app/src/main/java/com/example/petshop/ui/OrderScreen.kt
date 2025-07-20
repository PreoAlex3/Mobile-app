package com.example.petshop.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.database.Order
import com.example.petshop.data.database.OrderDao
import com.example.petshop.data.database.OrderItem
import com.example.petshop.data.database.Product
import com.example.petshop.viewmodel.OrdersViewModel
import com.example.petshop.viewmodel.OrdersViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.petshop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (Long) -> Unit = {},
    viewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModelFactory(
            DatabaseProvider.getOrderRepository(LocalContext.current),
            DatabaseProvider.getAuthRepository(LocalContext.current)
        )
    )
) {
    val orders by viewModel.orders.collectAsState()
    val authRepository = DatabaseProvider.getAuthRepository(LocalContext.current)
    val isLoggedIn = remember { authRepository.isLoggedIn() }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadOrders()
        }
    }


        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
            if (!isLoggedIn) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Orders",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please log in to view your orders",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (orders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "No Orders",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You don't have any orders yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your order history will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { orderWithItems ->
                        OrderCard(
                            order = orderWithItems,
                            onClick = { onOrderClick(orderWithItems.order.id) }
                        )
                    }
                }
            }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    order: OrderDao.OrderWithItems,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val orderDate = dateFormat.format(Date(order.order.orderDate))

    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ro", "RO"))
        .format(order.order.totalAmount)
        .replace("RON", "lei")

    // Status color based on order status
    val statusColor = when (order.order.status) {
        "DELIVERED" -> MaterialTheme.colorScheme.primary
        "SHIPPED" -> MaterialTheme.colorScheme.tertiary
        "PROCESSING" -> MaterialTheme.colorScheme.secondary
        "CANCELLED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order ID and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = orderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order summary
            Text(
                text = "${order.orderItems.size} items | $formattedTotal",
                style = MaterialTheme.typography.bodyMedium
            )

            // Sample of items (first 2)
            order.orderItems.take(2).forEach { orderItemWithProduct ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${orderItemWithProduct.orderItem.quantity}x",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = LocalContext.current.getString(orderItemWithProduct.product.nameResourceId),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (order.orderItems.size > 2) {
                Text(
                    text = "...and ${order.orderItems.size - 2} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = order.order.status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Payment method
                Text(
                    text = order.order.paymentMethod,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderScreenPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrderCard(
                    order = createSampleOrderWithItems(
                        id = 1001,
                        status = "DELIVERED",
                        totalAmount = 45.98,
                        paymentMethod = "Credit Card"
                    )
                )

                OrderCard(
                    order = createSampleOrderWithItems(
                        id = 1002,
                        status = "PROCESSING",
                        totalAmount = 79.96,
                        paymentMethod = "PayPal",
                        itemCount = 3
                    )
                )
            }
        }
    }
}

private fun createSampleOrderWithItems(
    id: Long,
    status: String,
    totalAmount: Double,
    paymentMethod: String,
    itemCount: Int = 2
): OrderDao.OrderWithItems {
    val order = Order(
        id = id,
        customerId = 1,
        orderDate = System.currentTimeMillis(),
        totalAmount = totalAmount,
        status = status,
        shippingAddress = "123 Main Street",
        paymentMethod = paymentMethod
    )

    val orderItems = (1..itemCount).map { i ->
        OrderDao.OrderItemWithProduct(
            orderItem = OrderItem(
                id = i.toLong(),
                orderId = id,
                productId = i.toLong(),
                quantity = 1,
                unitPrice = totalAmount / itemCount,
                totalPrice = totalAmount / itemCount
            ),
            product = Product(
                id = i.toLong(),
                categoryId = 1,
                nameResourceId = R.string.dog_food1_name,
                imageResourceId = R.drawable.dog_food1_photo,
                priceResourceId = R.string.dog_food1_price,
                price = totalAmount / itemCount,
                descriptionResourceId = R.string.dog_food1_description
            )
        )
    }

    return OrderDao.OrderWithItems(order, orderItems)
}