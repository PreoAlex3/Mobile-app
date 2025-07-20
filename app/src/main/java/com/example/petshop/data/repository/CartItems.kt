package com.example.petshop.data.repository

import androidx.room.Embedded
import androidx.room.Relation
import com.example.petshop.data.database.CartItem
import com.example.petshop.data.database.Product

data class CartItemWithProduct(
    @Embedded
    val cartItem: CartItem,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: Product
)