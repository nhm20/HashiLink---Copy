package com.example.hashilink.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hashilink.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SellerHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_seller_home, container, false)

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNavSeller)

        // Load default fragment
        loadChildFragment(SellerDashboardFragment())

        // Handle bottom navigation item selection
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_seller_home -> {
                    loadChildFragment(SellerDashboardFragment())
                    true
                }
                R.id.nav_my_products -> {
                    loadChildFragment(MyProductsFragment())
                    true
                }
                R.id.nav_seller_profile -> {
                    loadChildFragment(SellerProfileFragment())
                    true
                }
                else -> false
            }
        }
        return view
    }
    private fun loadChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.sellerFragmentContainer, fragment)
            .commit()
    }

    fun navigateToAddProduct() {
        loadChildFragment(AddProductFragment())
    }

    fun navigateToMyProducts() {
        loadChildFragment(MyProductsFragment())
        view?.findViewById<BottomNavigationView>(R.id.bottomNavSeller)?.selectedItemId = R.id.nav_my_products
    }
}