package com.example.petshop.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem(val title: String, val icon: ImageVector)
val tabs = listOf(
    TabItem("Home", Icons.Default.Home),
    TabItem("Products", Icons.Default.Search),
    TabItem("Cart", Icons.Default.ShoppingCart),
    TabItem("Account", Icons.Default.Person),
)