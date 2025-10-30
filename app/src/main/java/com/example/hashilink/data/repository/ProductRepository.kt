package com.example.hashilink.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hashilink.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProductRepository private constructor() {
    private val productsRef = FirebaseDatabase.getInstance().getReference("products")

    companion object {
        @Volatile
        private var INSTANCE: ProductRepository? = null
        
        // Cloudinary configuration - Replace with your credentials from cloudinary.com
        private const val CLOUD_NAME = "dzqbvwxzl"
        private const val API_KEY = "176456631438172"
        private const val API_SECRET = "ZruFVoJlT9VJR3Adk-Skkdg1-KU"
        
        fun getInstance(): ProductRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: ProductRepository().also { INSTANCE = it }
        }
        
        fun initCloudinary(context: Context) {
            try {
                val config = hashMapOf(
                    "cloud_name" to CLOUD_NAME,
                    "api_key" to API_KEY,
                    "api_secret" to API_SECRET
                )
                MediaManager.init(context, config)
            } catch (e: Exception) {
                // Already initialized
            }
        }
    }

    suspend fun uploadProductImage(imageUri: Uri, productId: String): Result<String> =
        suspendCoroutine { cont ->
            try {
                val requestId = MediaManager.get().upload(imageUri)
                    .option("folder", "hashilink/products")
                    .option("public_id", "${productId}_${System.currentTimeMillis()}")
                    .option("resource_type", "image")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            // Upload started
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            // Upload progress
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as? String
                            if (imageUrl != null) {
                                cont.resume(Result.success(imageUrl))
                            } else {
                                cont.resume(Result.failure(Exception("Failed to get image URL")))
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            cont.resume(Result.failure(Exception(error.description)))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            cont.resume(Result.failure(Exception("Upload rescheduled: ${error.description}")))
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                cont.resume(Result.failure(e))
            }
        }

    suspend fun addProduct(name: String, description: String, price: Double, quantity: Int, imageUrl: String = ""): Result<String> =
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

            val product = Product(key, name, description, price, quantity, currentUser.uid, imageUrl)

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

    suspend fun getAllProducts(): Result<List<Product>> =
        suspendCoroutine { cont ->
            productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
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