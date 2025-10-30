package com.example.hashilink

import android.app.Application
import com.example.hashilink.data.repository.ProductRepository

class HashiLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary
        ProductRepository.initCloudinary(this)
    }
}
