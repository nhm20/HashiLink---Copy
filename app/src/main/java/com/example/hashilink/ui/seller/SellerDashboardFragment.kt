package com.example.hashilink.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hashilink.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SellerDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false)

        val fabAddProduct = view.findViewById<FloatingActionButton>(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            // Navigate to Add Product fragment via parent's bottom nav
            (parentFragment as? SellerHomeFragment)?.navigateToAddProduct()
        }

        return view
    }
}