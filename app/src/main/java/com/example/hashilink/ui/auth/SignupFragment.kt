package com.example.hashilink.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hashilink.MainActivity
import com.example.hashilink.R
import com.example.hashilink.viewmodel.AuthViewModel

class SignupFragment : Fragment() {
    private lateinit var vm: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        vm = ViewModelProvider(this).get(AuthViewModel::class.java)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etMobile = view.findViewById<EditText>(R.id.etMobile)
        val roleSpinner = view.findViewById<Spinner>(R.id.roleSpinner)
        val btnSignup = view.findViewById<Button>(R.id.btnSignup)

        // Set spinner data
        val roles = arrayOf("buyer", "seller")
        roleSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        btnSignup.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "All fields are required",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            vm.signup(email, pass, name, mobile, role)
        }

        vm.loading.observe(viewLifecycleOwner) { isLoading ->
            btnSignup.isEnabled = !isLoading
            btnSignup.text = if (isLoading) "Creating Account..." else "Sign Up"
        }

        vm.authResult.observe(viewLifecycleOwner) {
            if (it.first) {
                Toast.makeText(requireContext(), "Signup success", Toast.LENGTH_SHORT).show()
                // Navigate to MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Signup failed: ${it.second}", Toast.LENGTH_LONG).show()
            }
        }

        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)
        tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}