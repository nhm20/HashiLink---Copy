package com.example.hashilink.ui.buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hashilink.R
import com.example.hashilink.data.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val ivPlaceholder: ImageView = itemView.findViewById(R.id.ivPlaceholder)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvSellerName: TextView = itemView.findViewById(R.id.tvSellerName)

        fun bind(order: Order) {
            tvProductName.text = order.productName
            tvQuantity.text = "Quantity: ${order.quantity}"
            tvTotalPrice.text = "₹${"%.2f".format(order.totalPrice)}"
            tvStatus.text = order.status
            tvSellerName.text = "Seller: ${order.sellerName}"

            // Format date
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvOrderDate.text = sdf.format(Date(order.orderDate))

            // Set status color
            when (order.status) {
                "Pending" -> tvStatus.setTextColor(itemView.context.getColor(R.color.accent_teal))
                "Confirmed" -> tvStatus.setTextColor(itemView.context.getColor(R.color.accent_purple))
                "Shipped" -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                "Delivered" -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                "Cancelled" -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            }

            // Load product image
            if (order.productImageUrl.isNotEmpty()) {
                ivPlaceholder.visibility = View.GONE
                ivProductImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(order.productImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivProductImage)
            } else {
                ivPlaceholder.visibility = View.VISIBLE
                ivProductImage.visibility = View.GONE
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
