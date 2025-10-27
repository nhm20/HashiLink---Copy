package com.example.hashilink.ui.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.example.hashilink.data.model.Product

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    var onItemClick: ((Product) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName = itemView.findViewById<TextView>(R.id.tvProductName)
        private val tvProductPrice = itemView.findViewById<TextView>(R.id.tvProductPrice)
        private val tvProductQuantity = itemView.findViewById<TextView>(R.id.tvProductQuantity)

        fun bind(product: Product) {
            tvProductName.text = product.name
            tvProductPrice.text = "₹${product.price}"
            tvProductQuantity.text = "Qty: ${product.quantity}"
            itemView.setOnClickListener {
                onItemClick?.invoke(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.productId == newItem.productId
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}