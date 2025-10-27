package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hashilink.R

class BuyerHomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_buyer_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation(view)
        // Load default home content
        loadFragment(BuyerHomeContentFragment())
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNav = view.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.buyer_bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Load Home content fragment
                    loadFragment(BuyerHomeContentFragment())
                    true
                }
                R.id.nav_orders -> {
                    // TODO: Load Orders fragment
                    true
                }
                R.id.nav_profile -> {
                    // Load Profile fragment
                    loadFragment(BuyerProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.buyer_fragment_container, fragment)
            .commit()
    }
}