package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.example.hashilink.data.model.Product
import com.example.hashilink.ui.seller.ProductAdapter
import com.example.hashilink.viewmodel.ProductViewModel

class BuyerHomeContentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_buyer_home_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProducts(view)
    }

    private fun setupProducts(view: View) {
        // Setup products RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ProductAdapter()
        recyclerView.adapter = adapter

        val productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        var allProducts: List<Product> = emptyList()
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            adapter.submitList(products)
        }
        productViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Optionally show/hide loading indicator
        }
        productViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            // Optionally show error message
        }
        // Load all products for buyers
        productViewModel.loadAllProducts()

        // Setup search functionality
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filteredProducts = if (query.isEmpty()) {
                    allProducts
                } else {
                    allProducts.filter { it.name.lowercase().contains(query) }
                }
                adapter.submitList(filteredProducts)
            }
        })
    }
}