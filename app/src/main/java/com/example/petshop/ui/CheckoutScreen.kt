package com.example.petshop.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.viewmodel.CheckoutViewModel
import com.example.petshop.viewmodel.CheckoutViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderComplete: () -> Unit,
    viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(
            cartRepository = DatabaseProvider.getCartRepository(LocalContext.current),
            orderRepository = DatabaseProvider.getOrderRepository(LocalContext.current),
            authRepository = DatabaseProvider.getAuthRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var shippingAddress by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Credit Card") }
    var notes by remember { mutableStateOf("") }

    val paymentOptions = listOf("Credit Card", "Cash on Delivery", "Bank Transfer")

    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var orderWasSuccessful by remember { mutableStateOf(false) }

    LaunchedEffect(orderWasSuccessful) {
        if (orderWasSuccessful) {
            onOrderComplete()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCartItems()
    }

    LaunchedEffect(Unit) {
        val user = DatabaseProvider.getAuthRepository(context).getCurrentUser()
        if (user != null) {
            shippingAddress = user.address
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Order Summary
                CheckoutSummaryCard(cartItems.size, cartTotal)

                Spacer(modifier = Modifier.height(16.dp))

                // Shipping Information
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Shipping Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            label = { Text("Shipping Address") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your complete address") },
                            minLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        paymentOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = paymentMethod == option,
                                    onClick = { paymentMethod = option }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Order Notes (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Additional Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Delivery instructions, preferences, etc.") },
                            minLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Place Order Button
                Button(
                    onClick = {
                        if (shippingAddress.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter a shipping address")
                            }
                            return@Button
                        }

                        coroutineScope.launch {
                            val result = viewModel.placeOrder(
                                shippingAddress = shippingAddress,
                                paymentMethod = paymentMethod,
                                notes = notes.ifBlank { null }
                            )

                            if (result.isSuccess) {
                                orderWasSuccessful = true
                            } else {
                                snackbarHostState.showSnackbar("Failed to place order: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                            }
                        }
                    },
                    enabled = !isProcessing && cartItems.isNotEmpty() && shippingAddress.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Place Order",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

}

@Composable
fun CheckoutSummaryCard(itemCount: Int, total: Double) {
    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ro", "RO"))
        .format(total)
        .replace("RON", "lei")

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items ($itemCount):",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Could add shipping fees here if needed

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    MaterialTheme {
        Surface {
            // Sample data for preview
            val sampleItemCount = 3
            val sampleTotal = 149.99

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Preview the summary card with sample data
                    CheckoutSummaryCard(itemCount = sampleItemCount, total = sampleTotal)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Basic shipping information card
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Shipping Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = "123 Main Street, City, Country",
                                onValueChange = { },
                                label = { Text("Shipping Address") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Place Order Button
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Place Order",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}