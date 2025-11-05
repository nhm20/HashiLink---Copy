package com.example.hashilink

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.hashilink.data.repository.ProductRepository
import com.example.hashilink.payment.StripePaymentHandler

class HashiLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Force light mode globally - disable dark theme completely
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        // Initialize Cloudinary
        ProductRepository.initCloudinary(this)
        // Initialize Stripe
        StripePaymentHandler.initialize(this)
    }
}
