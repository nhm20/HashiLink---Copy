package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hashilink.R
import com.example.hashilink.data.model.Product
import com.google.android.material.button.MaterialButton

class ProductDetailsFragment : Fragment() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getParcelable("product")!!
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

        val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
        val tvProductDescription = view.findViewById<TextView>(R.id.tvProductDescription)
        val tvProductPrice = view.findViewById<TextView>(R.id.tvProductPrice)
        val tvProductQuantity = view.findViewById<TextView>(R.id.tvProductQuantity)
        val tvSellerId = view.findViewById<TextView>(R.id.tvSellerId)
        val btnBuy = view.findViewById<MaterialButton>(R.id.btnBuy)

        tvProductName.text = product.name
        tvProductDescription.text = product.description
        tvProductPrice.text = "₹${product.price}"
        tvProductQuantity.text = "Available Quantity: ${product.quantity}"
        tvSellerId.text = "Seller ID: ${product.sellerId}"

        btnBuy.setOnClickListener {
            // TODO: Implement buy functionality
            Toast.makeText(requireContext(), "Buy functionality not implemented yet", Toast.LENGTH_SHORT).show()
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