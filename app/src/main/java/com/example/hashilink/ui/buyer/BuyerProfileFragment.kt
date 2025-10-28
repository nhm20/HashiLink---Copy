package com.example.hashilink.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hashilink.MainActivity
import com.example.hashilink.R
import com.example.hashilink.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class BuyerProfileFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private val TAG = "BuyerProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_buyer_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etMobile = view.findViewById<TextInputEditText>(R.id.etMobile)
        val etRole = view.findViewById<TextInputEditText>(R.id.etRole)
        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            authViewModel.getUserProfile(currentUser.uid)
        }

        authViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                etName.setText(user.name)
                etEmail.setText(user.email)
                etMobile.setText(user.mobile)
                etRole.setText(user.role)
                // TODO: Load profile image if available
            } else {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }

        btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
            Toast.makeText(requireContext(), "Edit Profile not implemented yet", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun performLogout() {
        try {
            Log.d(TAG, "Logging out user")
            
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
            
            Log.d(TAG, "User signed out successfully")
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            
            // Navigate back to MainActivity (which will show login screen)
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            Toast.makeText(requireContext(), "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}