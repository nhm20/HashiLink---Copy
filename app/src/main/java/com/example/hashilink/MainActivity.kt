package com.example.hashilink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hashilink.data.model.User
import com.example.hashilink.ui.auth.LoginFragment
import com.example.hashilink.ui.buyer.BuyerHomeFragment
import com.example.hashilink.ui.seller.SellerHomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private var userRole: String = "buyer" // Default to buyer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is logged in, fetch user role from Firebase Realtime Database
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    userRole = user?.role ?: "buyer" // Store the user role

                    val fragment = if (userRole == "seller") {
                        SellerHomeFragment()
                    } else {
                        BuyerHomeFragment()
                    }
                    loadFragment(fragment)
                }
                .addOnFailureListener {
                    // If failed to fetch role, default to buyer
                    userRole = "buyer"
                    loadFragment(BuyerHomeFragment())
                }
        } else {
            // User is not logged in, show login fragment
            loadFragment(LoginFragment())
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }
}