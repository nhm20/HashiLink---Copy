package com.example.hashilink.ui.seller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.example.hashilink.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth

class MyProductsFragment : Fragment() {
    private lateinit var viewModel: ProductViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val TAG = "MyProductsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_products, container, false)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProductAdapter()
        recyclerView.adapter = adapter

        // Observe products
        viewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d(TAG, "Products received: ${products.size}")
            adapter.submitList(products)
            if (products.isEmpty()) {
                Toast.makeText(requireContext(), "No products found. Add products to see them here.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e(TAG, "Error loading products: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        loadProducts()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh products when fragment becomes visible
        loadProducts()
    }

    private fun loadProducts() {
        // Load seller's products
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d(TAG, "Loading products for seller: ${currentUser.uid}")
            viewModel.loadSellerProducts(currentUser.uid)
        } else {
            Log.e(TAG, "No user logged in")
            Toast.makeText(requireContext(), "Please log in to view products", Toast.LENGTH_SHORT).show()
        }
    }
}