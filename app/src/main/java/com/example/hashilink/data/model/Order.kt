package com.example.hashilink.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImageUrl: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val orderDate: Long = 0L,
    val status: String = "Pending" // Pending, Confirmed, Shipped, Delivered, Cancelled
) : Parcelable
