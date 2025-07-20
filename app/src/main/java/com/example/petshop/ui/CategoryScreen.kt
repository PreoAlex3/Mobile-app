package com.example.petshop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.petshop.data.database.Category
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.viewmodel.CategoryViewModel
import com.example.petshop.viewmodel.CategoryViewModelFactory
import com.example.petshop.R
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
            .width(152.dp)
            .height(164.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
    ) {
        Column {
            Image(
                painter = painterResource(category.imageResourceId),
                contentDescription = stringResource(category.nameResourceId),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = stringResource(category.nameResourceId),
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
fun CategoryList(
    categories: List<Category>,
    onItemClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onItemClick(category) },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CategoryScreen(
    onCategoryClick: (Long) -> Unit,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            DatabaseProvider.getCategoryRepository(LocalContext.current)
        )
    )
) {
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryList(
            categories = categories,
            onItemClick = { category ->
                onCategoryClick(category.id)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    val mockCategories = listOf(
        Category(
            id = 1,
            name = "Dog",
            nameResourceId = R.string.Dog,
            imageResourceId = R.drawable.dog
        ),
        Category(
            id = 2,
            name = "Cat",
            nameResourceId = R.string.Cat,
            imageResourceId = R.drawable.cat
        ),
        Category(
            id = 3,
            name = "Bird",
            nameResourceId = R.string.Bird,
            imageResourceId = R.drawable.bird
        ),
        Category(
            id = 4,
            name = "Fish",
            nameResourceId = R.string.Fish,
            imageResourceId = R.drawable.fish
        )
    )

    // Create a preview-compatible version without extending the actual classes
    val previewFlow = MutableStateFlow(mockCategories)

    MaterialTheme {
        // Simulate the CategoryScreen by directly using CategoryList
        Column(modifier = Modifier.fillMaxSize()) {
            CategoryList(
                categories = mockCategories,
                onItemClick = { /* No-op for preview */ }
            )
        }
    }
}