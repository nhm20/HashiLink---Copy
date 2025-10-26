package com.example.hashilink.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hashilink.MainActivity
import com.example.hashilink.R
import com.example.hashilink.viewmodel.AuthViewModel

class LoginFragment : Fragment() {
    private lateinit var vm: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        vm = ViewModelProvider(this).get(AuthViewModel::class.java)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if(email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.login(email, pass)
        }

        vm.loading.observe(viewLifecycleOwner) { btnLogin.isEnabled = !it; btnLogin.text = if (it) "Logging in..." else "Login" }

        vm.authResult.observe(viewLifecycleOwner) { Toast.makeText(requireContext(), if (it.first) "Login success" else "Login failed: ${it.second}", Toast.LENGTH_SHORT).show() }
        vm.userRole.observe(viewLifecycleOwner) { role ->
            role?.let {
                startActivity(Intent(requireContext(), MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                requireActivity().finish()
            }
        }

        view.findViewById<TextView>(R.id.tvSignup).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.frameContainer, SignupFragment()).addToBackStack(null).commit()
        }

        return view
    }
}