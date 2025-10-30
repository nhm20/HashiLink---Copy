package com.example.hashilink.ui.buyer

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import com.bumptech.glide.Glide
    import com.example.hashilink.R
    import com.example.hashilink.data.model.Product
    import com.google.android.material.button.MaterialButton

    class ProductDetailsFragment : Fragment() {

        private lateinit var product: Product

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
                // TODO: Implement buy functionality
                Toast.makeText(requireContext(), "Buy functionality not implemented yet", Toast.LENGTH_SHORT).show()
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