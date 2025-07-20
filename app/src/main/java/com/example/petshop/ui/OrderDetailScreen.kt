package com.example.petshop.ui.orders

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.R
import com.example.petshop.data.database.Order
import com.example.petshop.data.database.OrderDao
import com.example.petshop.data.database.OrderItem
import com.example.petshop.data.database.Product
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = viewModel(
        factory = OrderDetailViewModelFactory(
            DatabaseProvider.getOrderRepository(LocalContext.current),
            orderId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is OrderDetailUiState.Loading -> LoadingView()
                is OrderDetailUiState.Error -> ErrorView(message = state.message)
                is OrderDetailUiState.Success -> OrderDetailContent(
                    orderWithItems = state.orderWithItems
                )
            }
        }

}

@Composable
fun OrderDetailContent(
    orderWithItems: OrderDao.OrderWithItems
) {
    val order = orderWithItems.order
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val orderDate = dateFormat.format(Date(order.orderDate))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Order Summary Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OrderInfoRow("Order Date", orderDate)
                    OrderInfoRow("Order Status", order.status)
                    OrderInfoRow("Payment Method", order.paymentMethod)
                    OrderInfoRow("Total Amount", "${order.totalAmount} lei")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Shipping Address Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Shipping Address",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.shippingAddress,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Order Items Section
        item {
            Text(
                text = "Order Items",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Display order items
        items(orderWithItems.orderItems) { orderItemWithProduct ->
            val orderItem = orderItemWithProduct.orderItem
            val product = orderItemWithProduct.product

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Properly display the product image
                    Image(
                        painter = painterResource(product.imageResourceId),
                        contentDescription = stringResource(id = product.nameResourceId),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = product.nameResourceId),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quantity: ${orderItem.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "${orderItem.totalPrice} lei",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Notes Section (if any)
        item {
            if (!order.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = order.notes,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Add some spacing at the bottom
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderDetailScreenPreview() {
    // Create sample data for preview
    val sampleProduct = Product(
        id = 1,
        categoryId = 1,
        nameResourceId = R.string.dog_food1_name,
        imageResourceId = R.drawable.dog_food1_photo,
        priceResourceId = R.string.dog_food1_price,
        price = 45.99,
        descriptionResourceId = R.string.dog_food1_description
    )

    val sampleOrderItem = OrderItem(
        id = 1,
        orderId = 1,
        productId = 1,
        quantity = 2,
        unitPrice = 45.99,
        totalPrice = 91.98
    )

    val sampleOrderItemWithProduct = OrderDao.OrderItemWithProduct(
        orderItem = sampleOrderItem,
        product = sampleProduct
    )

    val sampleOrder = Order(
        id = 1,
        customerId = 1,
        orderDate = System.currentTimeMillis(),
        totalAmount = 91.98,
        status = "DELIVERED",
        shippingAddress = "123 Main St, Anytown, CA 12345",
        paymentMethod = "Credit Card",
        notes = "Please leave package at the front door."
    )

    val sampleOrderWithItems = OrderDao.OrderWithItems(
        order = sampleOrder,
        orderItems = listOf(sampleOrderItemWithProduct)
    )

    MaterialTheme {
        OrderDetailContent(
            orderWithItems = sampleOrderWithItems
        )
    }
}