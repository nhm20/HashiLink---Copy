package com.example.hashilink.ui.seller

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hashilink.R
import com.example.hashilink.data.repository.ProductRepository
import com.example.hashilink.viewmodel.ProductViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {
    private val TAG = "AddProductFragment"
    private lateinit var viewModel: ProductViewModel
    private var selectedImageUri: Uri? = null
    private val productRepository = ProductRepository.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateImagePreview()
            }
        }
    }

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
        val cvProductImage = view.findViewById<MaterialCardView>(R.id.cvProductImage)
        val btnRemoveImage = view.findViewById<Button>(R.id.btnRemoveImage)

        // Image picker
        cvProductImage.setOnClickListener {
            openImagePicker()
        }

        btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            updateImagePreview()
        }

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

            // Show progress
            btnAddProduct.isEnabled = false
            btnAddProduct.text = "Adding..."

            // Upload image if selected, then add product
            lifecycleScope.launch {
                try {
                    var imageUrl = ""
                    
                    if (selectedImageUri != null) {
                        // Generate temporary product ID for image upload
                        val tempId = System.currentTimeMillis().toString()
                        val uploadResult = productRepository.uploadProductImage(selectedImageUri!!, tempId)
                        
                        if (uploadResult.isSuccess) {
                            imageUrl = uploadResult.getOrNull() ?: ""
                            Log.d(TAG, "Image uploaded: $imageUrl")
                        } else {
                            Log.e(TAG, "Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                            Toast.makeText(requireContext(), "Image upload failed, adding product without image", Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    // Add product with image URL
                    viewModel.addProduct(name, description, price, quantity, imageUrl)
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                    btnAddProduct.isEnabled = true
                    btnAddProduct.text = "Add Product"
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe result and loading from ProductViewModel
        viewModel.loading.observe(requireActivity()) { loading ->
            Log.d(TAG, "loading: $loading")
        }

        viewModel.addResult.observe(requireActivity()) { result ->
            result?.let {
                btnAddProduct.isEnabled = true
                btnAddProduct.text = "Add Product"
                
                if (it.isSuccess) {
                    val productId = it.getOrNull()
                    Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()

                    // Clear fields
                    view.findViewById<EditText>(R.id.etProductName).text?.clear()
                    view.findViewById<EditText>(R.id.etProductPrice).text?.clear()
                    view.findViewById<EditText>(R.id.etProductDescription).text?.clear()
                    view.findViewById<EditText>(R.id.etProductQuantity).text?.clear()
                    selectedImageUri = null
                    updateImagePreview()

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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun updateImagePreview() {
        val ivProductImage = view?.findViewById<ImageView>(R.id.ivProductImage)
        val llPlaceholder = view?.findViewById<LinearLayout>(R.id.llImagePlaceholder)
        val btnRemoveImage = view?.findViewById<Button>(R.id.btnRemoveImage)

        if (selectedImageUri != null) {
            ivProductImage?.visibility = View.VISIBLE
            llPlaceholder?.visibility = View.GONE
            btnRemoveImage?.visibility = View.VISIBLE
            
            Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .into(ivProductImage!!)
        } else {
            ivProductImage?.visibility = View.GONE
            llPlaceholder?.visibility = View.VISIBLE
            btnRemoveImage?.visibility = View.GONE
        }
    }
}