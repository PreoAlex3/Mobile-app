package com.example.petshop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.database.Product
import com.example.petshop.viewmodel.CartViewModel
import com.example.petshop.viewmodel.CartViewModelFactory
import com.example.petshop.viewmodel.ProductViewModel
import com.example.petshop.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.petshop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productId: Int,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(
            DatabaseProvider.getProductRepository(LocalContext.current)
        )
    ),
    cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(
            DatabaseProvider.getCartRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val authRepository = DatabaseProvider.getAuthRepository(context)
    val isLoggedIn = remember { authRepository.isLoggedIn() }

    val coroutineScope = rememberCoroutineScope()
    var product by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    // State for notification/popup
    var showTopNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }
    var notificationActionLabel by remember { mutableStateOf("") }
    var onNotificationAction by remember { mutableStateOf({}) }

    // Load product details
    LaunchedEffect(productId) {
        coroutineScope.launch {
            product = productViewModel.getProductById(productId.toLong())
        }
    }

    // Auto-dismiss notification after delay
    LaunchedEffect(showTopNotification) {
        if (showTopNotification) {
            delay(3000) // Auto-dismiss after 3 seconds
            showTopNotification = false
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                product?.let { currentProduct ->
                    // Product Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = currentProduct.imageResourceId),
                            contentDescription = "Product Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    // Product Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = currentProduct.nameResourceId),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price
                        Text(
                            text = stringResource(id = currentProduct.priceResourceId),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Description
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = currentProduct.descriptionResourceId),
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Visible
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quantity selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Quantity:",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            FilledIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(text = "-")
                            }

                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            FilledIconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(text = "+")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Add to cart button with authentication check
                        Button(
                            onClick = {
                                if (isLoggedIn) {
                                    // User is logged in, proceed with adding to cart
                                    coroutineScope.launch {
                                        val currentCustomerId = authRepository.getCurrentUserId() ?: 1
                                        cartViewModel.addToCart(
                                            customerId = currentCustomerId,
                                            productId = currentProduct.id,
                                            quantity = quantity
                                        )

                                        // Show product added notification
                                        notificationMessage = "Product added to cart"
                                        notificationActionLabel = "View Cart"
                                        onNotificationAction = { onCartClick() }
                                        showTopNotification = true
                                    }
                                } else {
                                    // User is not logged in, show authentication notification
                                    notificationMessage = "Sign in to add items"
                                    notificationActionLabel = "Sign In"
                                    onNotificationAction = { onLoginClick() }
                                    showTopNotification = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add to Cart",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } ?: run {
                    // Show loading or product not found
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Top notification/popup
            AnimatedVisibility(
                visible = showTopNotification,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notificationMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        TextButton(
                            onClick = {
                                onNotificationAction()
                                showTopNotification = false
                            }
                        ) {
                            Text(
                                text = notificationActionLabel,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

}

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    // Create a sample product for preview
    val sampleProduct = Product(
        id = 1,
        categoryId = 1,
        nameResourceId = R.string.dog_food1_name,
        imageResourceId = R.drawable.dog_food1_photo,
        priceResourceId = R.string.dog_food1_price,
        price = 25.99,
        descriptionResourceId = R.string.dog_food1_description
    )

    // Set up a preview theme
    MaterialTheme {
        // Create a simplified version of ProductScreen that doesn't need real data
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = sampleProduct.imageResourceId),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Product Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Premium Dog Food",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price
                    Text(
                        text = "$25.99",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "High-quality dog food with premium ingredients. Perfect for adult dogs of all breeds.",
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Visible
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Quantity:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        FilledIconButton(
                            onClick = { },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(text = "-")
                        }

                        Text(
                            text = "1",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        FilledIconButton(
                            onClick = { },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(text = "+")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Add to cart button
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to Cart",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}