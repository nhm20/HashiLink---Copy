package com.example.hashilink.data.repository

import com.example.hashilink.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OrderRepository private constructor() {
    private val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    companion object {
        @Volatile
        private var INSTANCE: OrderRepository? = null
        fun getInstance(): OrderRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: OrderRepository().also { INSTANCE = it }
        }
    }

    suspend fun placeOrder(
        productId: String,
        productName: String,
        productPrice: Double,
        productImageUrl: String,
        quantity: Int,
        sellerId: String
    ): Result<String> = suspendCoroutine { cont ->
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            android.util.Log.e("OrderRepository", "User not logged in")
            cont.resume(Result.failure(Exception("User not logged in")))
            return@suspendCoroutine
        }

        android.util.Log.d("OrderRepository", "Placing order for product: $productName, buyer: ${currentUser.uid}, seller: $sellerId")

        // Create order immediately without fetching names first
        val newRef = ordersRef.push()
        val orderId = newRef.key
        if (orderId == null) {
            android.util.Log.e("OrderRepository", "Failed to generate order ID")
            cont.resume(Result.failure(Exception("Failed to generate order ID")))
            return@suspendCoroutine
        }

        // Create order with UIDs first, will update names later if needed
        val order = Order(
            orderId = orderId,
            productId = productId,
            productName = productName,
            productPrice = productPrice,
            productImageUrl = productImageUrl,
            quantity = quantity,
            totalPrice = productPrice * quantity,
            buyerId = currentUser.uid,
            buyerName = currentUser.email ?: "Buyer",
            sellerId = sellerId,
            sellerName = "Seller",
            orderDate = System.currentTimeMillis(),
            status = "Pending"
        )

        android.util.Log.d("OrderRepository", "Saving order to Firebase: $orderId")
        newRef.setValue(order)
            .addOnSuccessListener {
                android.util.Log.d("OrderRepository", "Order saved successfully: $orderId")
                
                // Update with actual names in background (optional)
                usersRef.child(currentUser.uid).child("name").get()
                    .addOnSuccessListener { buyerSnapshot ->
                        val buyerName = buyerSnapshot.getValue(String::class.java)
                        if (buyerName != null) {
                            newRef.child("buyerName").setValue(buyerName)
                        }
                    }
                
                usersRef.child(sellerId).child("name").get()
                    .addOnSuccessListener { sellerSnapshot ->
                        val sellerName = sellerSnapshot.getValue(String::class.java)
                        if (sellerName != null) {
                            newRef.child("sellerName").setValue(sellerName)
                        }
                    }
                
                cont.resume(Result.success(orderId))
            }
            .addOnFailureListener { ex ->
                android.util.Log.e("OrderRepository", "Failed to save order: ${ex.message}", ex)
                cont.resume(Result.failure(ex))
            }
    }

    suspend fun getBuyerOrders(buyerId: String): Result<List<Order>> =
        suspendCoroutine { cont ->
            val query = ordersRef.orderByChild("buyerId").equalTo(buyerId)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = mutableListOf<Order>()
                        for (child in snapshot.children) {
                            val order = child.getValue(Order::class.java)
                            if (order != null) list.add(order)
                        }
                        // Sort by order date descending (newest first)
                        list.sortByDescending { it.orderDate }
                        cont.resume(Result.success(list))
                    } catch (e: Exception) {
                        cont.resume(Result.failure(e))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resume(Result.failure(Exception(error.message)))
                }
            })
        }

    suspend fun getSellerOrders(sellerId: String): Result<List<Order>> =
        suspendCoroutine { cont ->
            val query = ordersRef.orderByChild("sellerId").equalTo(sellerId)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = mutableListOf<Order>()
                        for (child in snapshot.children) {
                            val order = child.getValue(Order::class.java)
                            if (order != null) list.add(order)
                        }
                        // Sort by order date descending (newest first)
                        list.sortByDescending { it.orderDate }
                        cont.resume(Result.success(list))
                    } catch (e: Exception) {
                        cont.resume(Result.failure(e))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resume(Result.failure(Exception(error.message)))
                }
            })
        }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> =
        suspendCoroutine { cont ->
            ordersRef.child(orderId).child("status").setValue(status)
                .addOnSuccessListener {
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { ex ->
                    cont.resume(Result.failure(ex))
                }
        }
}
