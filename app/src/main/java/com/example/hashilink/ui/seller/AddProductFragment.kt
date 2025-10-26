package com.example.hashilink.ui.seller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hashilink.R
import com.example.hashilink.viewmodel.ProductViewModel

class AddProductFragment : Fragment() {
    private val TAG = "AddProductFragment"
    private lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_product, container, false)

        // Use activity-scoped ViewModel so product list fragment can observe same ViewModel if desired
        viewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)

        val etProductName = view.findViewById<EditText>(R.id.etProductName)
        val etProductPrice = view.findViewById<EditText>(R.id.etProductPrice)
        val etProductDescription = view.findViewById<EditText>(R.id.etProductDescription)
        val etProductQuantity = view.findViewById<EditText>(R.id.etProductQuantity)
        val btnAddProduct = view.findViewById<Button>(R.id.btnAddProduct)

        btnAddProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val priceText = etProductPrice.text.toString().trim()
            val description = etProductDescription.text.toString().trim()
            val quantityText = etProductQuantity.text.toString().trim()

            if (name.isEmpty() || priceText.isEmpty() || description.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            val quantity = quantityText.toIntOrNull()

            if (price == null || price < 0.0) {
                Toast.makeText(requireContext(), "Enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (quantity == null || quantity < 0) {
                Toast.makeText(requireContext(), "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use ProductViewModel to perform addProduct
            viewModel.addProduct(name, description, price, quantity)
        }

        // Observe result and loading from ProductViewModel
        viewModel.loading.observe(requireActivity()) { loading ->
            Log.d(TAG, "loading: $loading")
        }

        viewModel.addResult.observe(requireActivity()) { result ->
            result?.let {
                if (it.isSuccess) {
                    val productId = it.getOrNull()
                    Toast.makeText(requireContext(), "Product added: $productId", Toast.LENGTH_SHORT).show()

                    // Clear fields
                    view.findViewById<EditText>(R.id.etProductName).text?.clear()
                    view.findViewById<EditText>(R.id.etProductPrice).text?.clear()
                    view.findViewById<EditText>(R.id.etProductDescription).text?.clear()
                    view.findViewById<EditText>(R.id.etProductQuantity).text?.clear()

                    (parentFragment as? SellerHomeFragment)?.navigateToMyProducts()
                } else {
                    val ex = it.exceptionOrNull()
                    Toast.makeText(requireContext(), "Failed: ${ex?.message}", Toast.LENGTH_SHORT).show()
                }
                // reset addResult
                viewModel.clearAddResult()
            }
        }

        return view
    }
}