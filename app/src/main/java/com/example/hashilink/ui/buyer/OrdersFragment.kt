package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.example.hashilink.ui.viewmodel.OrderViewModel
import com.google.firebase.auth.FirebaseAuth

class OrdersFragment : Fragment() {

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoOrders: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)

        rvOrders = view.findViewById(R.id.rvOrders)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoOrders = view.findViewById(R.id.tvNoOrders)

        orderAdapter = OrderAdapter()
        rvOrders.layoutManager = LinearLayoutManager(requireContext())
        rvOrders.adapter = orderAdapter

        // Observe loading state
        orderViewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Observe orders
        orderViewModel.buyerOrders.observe(viewLifecycleOwner) { result ->
            result.onSuccess { orders ->
                if (orders.isEmpty()) {
                    tvNoOrders.visibility = View.VISIBLE
                    rvOrders.visibility = View.GONE
                } else {
                    tvNoOrders.visibility = View.GONE
                    rvOrders.visibility = View.VISIBLE
                    orderAdapter.submitList(orders)
                }
            }.onFailure { ex ->
                Toast.makeText(requireContext(), "Failed to load orders: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Load orders
        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        // Refresh orders when fragment becomes visible
        loadOrders()
    }

    private fun loadOrders() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            orderViewModel.loadBuyerOrders(currentUser.uid)
        }
    }

    companion object {
        fun newInstance(): OrdersFragment {
            return OrdersFragment()
        }
    }
}
