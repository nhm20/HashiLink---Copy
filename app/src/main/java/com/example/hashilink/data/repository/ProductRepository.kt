package com.example.hashilink.data.repository

import com.example.hashilink.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProductRepository private constructor() {
    private val productsRef = FirebaseDatabase.getInstance().getReference("products")

    companion object {
        @Volatile
        private var INSTANCE: ProductRepository? = null
        fun getInstance(): ProductRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: ProductRepository().also { INSTANCE = it }
        }
    }

    suspend fun addProduct(name: String, description: String, price: Double, quantity: Int): Result<String> =
        suspendCoroutine { cont ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                cont.resume(Result.failure(Exception("User not logged in")))
                return@suspendCoroutine
            }

            val newRef = productsRef.push()
            val key = newRef.key
            if (key == null) {
                cont.resume(Result.failure(Exception("Failed to generate product id")))
                return@suspendCoroutine
            }

            val product = Product(key, name, description, price, quantity, currentUser.uid)

            newRef.setValue(product)
                .addOnSuccessListener {
                    cont.resume(Result.success(key))
                }
                .addOnFailureListener { ex ->
                    cont.resume(Result.failure(ex))
                }
        }

    suspend fun getSellerProducts(sellerId: String): Result<List<Product>> =
        suspendCoroutine { cont ->
            val query = productsRef.orderByChild("sellerId").equalTo(sellerId)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = mutableListOf<Product>()
                        for (child in snapshot.children) {
                            val p = child.getValue(Product::class.java)
                            if (p != null) list.add(p)
                        }
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
}