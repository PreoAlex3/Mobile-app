package com.example.petshop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.R
import com.example.petshop.data.database.Category
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.database.Product
import com.example.petshop.ui.theme.PetShopTheme
import com.example.petshop.viewmodel.CategoryViewModel
import com.example.petshop.viewmodel.CategoryViewModelFactory
import com.example.petshop.viewmodel.ProductViewModel
import com.example.petshop.viewmodel.ProductViewModelFactory

interface CardItem {
    val imageResourceId : Int
    val stringResourceId : Int
}

@Composable
fun GenericCard(
    item : CardItem,
    onClick : () -> Unit = {},
    modifier : Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
            .width(152.dp)
            .height(164.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
    ) {
        Column {
            Image(
                painterResource(item.imageResourceId),
                contentDescription = stringResource(item.stringResourceId),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = stringResource(item.stringResourceId),
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        }
    }
}

@Composable
fun <T> GenericList(
    items : List<T>,
    itemToCardItem : (T) -> CardItem,
    onItemClick : (T) -> Unit = {},
    modifier : Modifier = Modifier,
    maxItems: Int = Int.MAX_VALUE // Added parameter to limit items
) {
    // Take only the first maxItems from the list
    val limitedItems = items.take(maxItems)

    LazyRow(
        modifier = modifier
            .padding(horizontal = 4.dp)
    ) {
        items(limitedItems) { item ->
            GenericCard(
                item = itemToCardItem(item),
                onClick = { onItemClick(item) },
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ProductList(
    products: List<Product>,
    onProductClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    maxItems: Int = 2
) {
    GenericList(
        items = products,
        itemToCardItem = { product ->
            object : CardItem {
                override val imageResourceId = product.imageResourceId
                override val stringResourceId = product.nameResourceId
            }
        },
        onItemClick = { product ->
            onProductClick(product.id)
        },
        modifier = modifier,
        maxItems = maxItems
    )
}

@Composable
fun HomeScreen(
    onCategoryClick: (Long) -> Unit = {},
    onProductClick: (Long) -> Unit = {},
    categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            DatabaseProvider.getCategoryRepository(LocalContext.current)
        )
    ),
    productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(
            DatabaseProvider.getProductRepository(LocalContext.current)
        )
    )
) {
    val categories by categoryViewModel.categories.collectAsState(initial = emptyList())

    var dogFoodProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var catFoodProducts by remember { mutableStateOf<List<Product>>(emptyList()) }

    val dogFoodCategoryId = 1L
    val catFoodCategoryId = 2L

    LaunchedEffect(Unit) {
        productViewModel.getProductsByCategory(dogFoodCategoryId).collect { products ->
            dogFoodProducts = products
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.getProductsByCategory(catFoodCategoryId).collect { products ->
            catFoodProducts = products
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        GenericList<Category>(
            items = categories,
            itemToCardItem = { category ->
                object : CardItem {
                    override val imageResourceId = category.imageResourceId
                    override val stringResourceId = category.nameResourceId
                }
            },
            onItemClick = { category ->
                onCategoryClick(category.id)
            },
            maxItems = 4
        )

        Text(
            text = "Dog food",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        ProductList(
            products = dogFoodProducts,
            onProductClick = onProductClick,
            maxItems = 5
        )

        Text(
            text = "Cat food",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        ProductList(
            products = catFoodProducts,
            onProductClick = onProductClick,
            maxItems = 5
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetShopTheme {
        val mockCategories = listOf(
            MockCategory(1, R.drawable.dog, R.string.category_dogs),
            MockCategory(2, R.drawable.cat, R.string.category_cats),
            MockCategory(3, R.drawable.bird, R.string.category_birds),
            MockCategory(4, R.drawable.fish, R.string.category_fish)
        )

        val mockDogFood = listOf(
            MockProduct(1, R.drawable.dog_food1_photo, R.string.dog_food1_name),
            MockProduct(2, R.drawable.dog_food2_photo, R.string.dog_food2_name),
            MockProduct(3, R.drawable.dog_food3_photo, R.string.dog_food3_name)
        )

        val mockCatFood = listOf(
            MockProduct(4, R.drawable.cat_food1_photo, R.string.cat_food1_name),
            MockProduct(5, R.drawable.cat_food2_photo, R.string.cat_food2_name),
            MockProduct(6, R.drawable.cat_food3_photo, R.string.cat_food3_name)
        )

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Categories section
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            GenericList(
                items = mockCategories,
                itemToCardItem = { category ->
                    object : CardItem {
                        override val imageResourceId = category.imageRes
                        override val stringResourceId = category.nameRes
                    }
                },
                maxItems = 4
            )

            // Dog food section
            Text(
                text = "Dog Food",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            GenericList(
                items = mockDogFood,
                itemToCardItem = { product ->
                    object : CardItem {
                        override val imageResourceId = product.imageRes
                        override val stringResourceId = product.nameRes
                    }
                },
                maxItems = 5
            )

            // Cat food section
            Text(
                text = "Cat Food",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            GenericList(
                items = mockCatFood,
                itemToCardItem = { product ->
                    object : CardItem {
                        override val imageResourceId = product.imageRes
                        override val stringResourceId = product.nameRes
                    }
                },
                maxItems = 5
            )
        }
    }
}

// Simple data classes for mocking in the preview
private data class MockCategory(
    val id: Long,
    val imageRes: Int,
    val nameRes: Int
)

private data class MockProduct(
    val id: Long,
    val imageRes: Int,
    val nameRes: Int
)