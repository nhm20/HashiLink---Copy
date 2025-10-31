package com.example.hashilink.ui.buyer

    import android.app.AlertDialog
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.ViewModelProvider
    import com.bumptech.glide.Glide
    import com.example.hashilink.R
    import com.example.hashilink.data.model.Product
    import com.example.hashilink.ui.viewmodel.OrderViewModel
    import com.google.android.material.button.MaterialButton

    class ProductDetailsFragment : Fragment() {

        private lateinit var product: Product
        private lateinit var orderViewModel: OrderViewModel

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                product = it.getParcelable<Product>("product") ?: throw IllegalArgumentException("Product not found in arguments")
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.fragment_product_details, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)

            val ivProductImage = view.findViewById<ImageView>(R.id.ivProductImage)
            val ivPlaceholder = view.findViewById<ImageView>(R.id.ivPlaceholder)
            val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
            val tvProductDescription = view.findViewById<TextView>(R.id.tvProductDescription)
            val tvProductPrice = view.findViewById<TextView>(R.id.tvProductPrice)
            val tvProductQuantity = view.findViewById<TextView>(R.id.tvProductQuantity)
            val tvSellerId = view.findViewById<TextView>(R.id.tvSellerId)
            val btnBuy = view.findViewById<MaterialButton>(R.id.btnBuy)
            val btnChat = view.findViewById<MaterialButton>(R.id.btnChat)

            tvProductName.text = product.name
            tvProductDescription.text = product.description
            tvProductPrice.text = getString(R.string.product_price, product.price)
            tvProductQuantity.text = getString(R.string.product_quantity, product.quantity)
            tvSellerId.text = getString(R.string.seller_id, product.sellerId)

            // Load product image
            if (product.imageUrl.isNotEmpty()) {
                ivPlaceholder.visibility = View.GONE
                ivProductImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(product.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivProductImage)
            } else {
                ivPlaceholder.visibility = View.VISIBLE
                ivProductImage.visibility = View.GONE
            }

            btnBuy.setOnClickListener {
                showBuyDialog()
            }

            // Observe order placement result
            orderViewModel.placeOrderResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess { orderId ->
                    android.util.Log.d("ProductDetails", "Order placed successfully with ID: $orderId")
                    Toast.makeText(requireContext(), "Order placed successfully! Check Orders tab to view.", Toast.LENGTH_LONG).show()
                    // Navigate back to home
                    parentFragmentManager.popBackStack()
                }.onFailure { ex ->
                    android.util.Log.e("ProductDetails", "Failed to place order", ex)
                    Toast.makeText(requireContext(), "Failed to place order: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }

            btnChat.setOnClickListener {
                // Fetch seller name from database
                val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                database.getReference("users").child(product.sellerId).get().addOnSuccessListener { snapshot ->
                    val sellerName = snapshot.child("name").getValue(String::class.java) ?: "Seller"
                    val chatFragment = ChatFragment.newInstance(product.sellerId, sellerName)
                    (parentFragment as? BuyerHomeFragment)?.loadFragment(chatFragment)
                }.addOnFailureListener {
                    // Fallback to showing chat without name
                    val chatFragment = ChatFragment.newInstance(product.sellerId, "Seller")
                    (parentFragment as? BuyerHomeFragment)?.loadFragment(chatFragment)
                }
            }
        }

        private fun showBuyDialog() {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_buy_product, null)
            val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
            val tvTotalPrice = dialogView.findViewById<TextView>(R.id.tvTotalPrice)

            etQuantity.setText("1")
            tvTotalPrice.text = "Total: ₹${product.price}"

            etQuantity.setOnKeyListener { _, _, _ ->
                val qty = etQuantity.text.toString().toIntOrNull() ?: 1
                val total = product.price * qty
                tvTotalPrice.text = "Total: ₹${"%.2f".format(total)}"
                false
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Buy ${product.name}")
                .setView(dialogView)
                .setPositiveButton("Confirm Purchase") { _, _ ->
                    val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
                    if (quantity <= 0) {
                        Toast.makeText(requireContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (quantity > product.quantity) {
                        Toast.makeText(requireContext(), "Not enough stock available", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    orderViewModel.placeOrder(
                        productId = product.productId,
                        productName = product.name,
                        productPrice = product.price,
                        productImageUrl = product.imageUrl,
                        quantity = quantity,
                        sellerId = product.sellerId
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        companion object {
            fun newInstance(product: Product): ProductDetailsFragment {
                val fragment = ProductDetailsFragment()
                val args = Bundle()
                args.putParcelable("product", product)
                fragment.arguments = args
                return fragment
            }
        }
    }