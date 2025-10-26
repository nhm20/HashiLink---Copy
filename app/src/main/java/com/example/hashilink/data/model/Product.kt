package com.example.hashilink.data.model

data class Product(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val sellerId: String = ""
)