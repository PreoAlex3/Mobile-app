package com.example.petshop.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.CartItem
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.database.Product
import com.example.petshop.data.repository.CartItemWithProduct
import com.example.petshop.viewmodel.CartViewModel
import com.example.petshop.viewmodel.CartViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import com.example.petshop.R

@Composable
fun CartItemCard(
    cartItem: CartItemWithProduct,
    modifier: Modifier = Modifier,
    onRemoveItem: () -> Unit = {},
    onQuantityChange: (Int) -> Unit = {},
) {
    var quantity by remember { mutableStateOf(cartItem.cartItem.quantity) }
    val context = LocalContext.current

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                // Product image
                Image(
                    painter = painterResource(cartItem.product.imageResourceId),
                    contentDescription = stringResource(cartItem.product.nameResourceId),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = stringResource(cartItem.product.nameResourceId),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom row with controls and price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (quantity > 1) {
                                quantity--
                                onQuantityChange(quantity)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.widthIn(min = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            quantity++
                            onQuantityChange(quantity)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onRemoveItem,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Price calculation
                val totalPrice = cartItem.product.price * quantity
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("ro", "RO"))
                    .format(totalPrice)
                    .replace("RON", "lei")

                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CartItemsList(
    cartItems: List<CartItemWithProduct>,
    modifier: Modifier = Modifier,
    onRemoveItem: (CartItemWithProduct) -> Unit = {},
    onQuantityChange: (CartItemWithProduct, Int) -> Unit = { _, _ -> },
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cartItems) { cartItemWithProduct ->
            CartItemCard(
                cartItem = cartItemWithProduct,
                onRemoveItem = { onRemoveItem(cartItemWithProduct) },
                onQuantityChange = { newQuantity ->
                    onQuantityChange(cartItemWithProduct, newQuantity)
                }
            )
        }
    }
}

@Composable
fun CartTotalAndCheckout(
    total: Double,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ro", "RO"))
        .format(total)
        .replace("RON", "lei")

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Total section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkout button
            Button(
                onClick = onCheckoutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBrowseClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    navigateToCategory: (String) -> Unit = {},
    viewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(
            DatabaseProvider.getCartRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val authRepository = DatabaseProvider.getAuthRepository(context)
    val isLoggedIn = remember { authRepository.isLoggedIn() }

    // Get current user ID if logged in, or use a placeholder/guest ID
    val currentCustomerId = remember {
        if (isLoggedIn) authRepository.getCurrentUserId() ?: 1L
        else -1L  // Use a placeholder/invalid ID for non-logged-in users
    }

    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Load cart items when screen is first displayed and when auth state changes
    LaunchedEffect(currentCustomerId, isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadCartItems(currentCustomerId)
        } else {
            // Clear the displayed cart if not logged in
            viewModel.clearCartDisplay()
        }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (!isLoggedIn) {
                    // Not logged in state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Login required",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please log in to view your cart",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You need to be logged in to add and view items in your cart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                } else if (cartItems.isEmpty()) {
                    // Empty cart state (logged in but cart is empty)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Your existing empty cart UI
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Empty cart",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your cart is empty",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Browse our pet products to add items to your cart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBrowseClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text("Browse Products")
                        }
                    }
                } else {
                    // Cart with items
                    CartItemsList(
                        cartItems = cartItems,
                        onRemoveItem = { cartItemWithProduct ->
                            coroutineScope.launch {
                                viewModel.removeFromCart(cartItemWithProduct.cartItem)
                            }
                        },
                        onQuantityChange = { cartItemWithProduct, quantity ->
                            coroutineScope.launch {
                                viewModel.updateQuantity(cartItemWithProduct.cartItem.id, quantity)
                            }
                        }
                    )
                }
            }

            // Position the payment section at the bottom
            if (isLoggedIn && cartItems.isNotEmpty()) {
                CartTotalAndCheckout(
                    total = cartTotal,
                    onCheckoutClick = onCheckoutClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    MaterialTheme {
        // Create sample products based on your actual database schema
        val product1 = Product(
            id = 1L,
            categoryId = 1L,  // Dog category
            nameResourceId = R.string.dog_food1_name,
            imageResourceId = R.drawable.dog_food1_photo,
            priceResourceId = R.string.dog_food1_price,
            price = 59.99,
            descriptionResourceId = R.string.dog_food1_description
        )

        val product2 = Product(
            id = 2L,
            categoryId = 2L,  // Cat category
            nameResourceId = R.string.cat_food1_name,
            imageResourceId = R.drawable.cat_food1_photo,
            priceResourceId = R.string.cat_food1_price,
            price = 29.99,
            descriptionResourceId = R.string.cat_food1_description
        )

        // Create sample CartItemWithProduct objects that match your repository data structure
        val cartItems = listOf(
            CartItemWithProduct(
                cartItem = CartItem(
                    id = 1L,
                    customerId = 1L,
                    productId = 1L,
                    quantity = 2,
                    dateAdded = System.currentTimeMillis()
                ),
                product = product1
            ),
            CartItemWithProduct(
                cartItem = CartItem(
                    id = 2L,
                    customerId = 1L,
                    productId = 2L,
                    quantity = 1,
                    dateAdded = System.currentTimeMillis() - 86400000 // One day ago
                ),
                product = product2
            )
        )

        // Calculate total for preview
        val cartTotal = cartItems.sumOf { it.product.price * it.cartItem.quantity }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Show cart items list
                CartItemsList(
                    cartItems = cartItems,
                    onRemoveItem = { },
                    onQuantityChange = { _, _ -> }
                )
            }

            // Show total and checkout section at bottom
            CartTotalAndCheckout(
                total = cartTotal,
                onCheckoutClick = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}