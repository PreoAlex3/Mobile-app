package com.example.petshop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.database.Product
import com.example.petshop.viewmodel.ProductViewModel
import com.example.petshop.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.petshop.R

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(product.imageResourceId),
                contentDescription = stringResource(product.nameResourceId),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(product.nameResourceId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(product.priceResourceId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProductListScreen(
    categoryId: Long,
    onProductClick: (Long) -> Unit,
    viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(
            DatabaseProvider.getProductRepository(LocalContext.current)
        )
    )
) {
    // Load products for the selected category
    viewModel.loadProductsByCategory(categoryId)
    val products by viewModel.products.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = { onProductClick(product.id) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductListScreenPreview() {
    // Sample product data for the preview
    val sampleProducts = listOf(
        Product(
            id = 1,
            categoryId = 1,
            nameResourceId = R.string.dog_food1_name,
            imageResourceId = R.drawable.dog_food1_photo,
            priceResourceId = R.string.dog_food1_price,
            price = 25.99,
            descriptionResourceId = R.string.dog_food1_description
        ),
        Product(
            id = 2,
            categoryId = 1,
            nameResourceId = R.string.dog_food2_name,
            imageResourceId = R.drawable.dog_food2_photo,
            priceResourceId = R.string.dog_food2_price,
            price = 19.99,
            descriptionResourceId = R.string.dog_food2_description
        ),
        Product(
            id = 3,
            categoryId = 2,
            nameResourceId = R.string.cat_food1_name,
            imageResourceId = R.drawable.cat_food1_photo,
            priceResourceId = R.string.cat_food1_price,
            price = 15.50,
            descriptionResourceId = R.string.cat_food1_description
        )
    )

    // Create a simplified ProductViewModel for the preview
    val previewViewModel = object {
        val products = MutableStateFlow(sampleProducts)
        fun loadProductsByCategory(categoryId: Long) {
            // No-op for preview
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Display a header for the preview
            Text(
                text = "Pet Food Products",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Display product items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleProducts) { product ->
                    ProductItem(
                        product = product,
                        onClick = { /* No-op for preview */ },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}