package com.example.hashilink.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hashilink.MainActivity
import com.example.hashilink.R
import com.example.hashilink.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class SellerProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_seller_profile, container, false)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvMobile = view.findViewById<TextView>(R.id.tvMobile)
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Observe user profile data
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvName.text = "Name: ${it.name}"
                tvEmail.text = "Email: ${it.email}"
                tvMobile.text = "Mobile: ${it.mobile}"
                tvRole.text = "Role: ${it.role}"
            }
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a progress bar here if needed
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Load user profile
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            viewModel.loadUserProfile(user.uid)
        }

        // Logout button
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}