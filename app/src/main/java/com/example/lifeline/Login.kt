package com.example.lifeline

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.lifeline.databinding.FragmentLoginBinding
import com.google.gson.reflect.TypeToken

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.button7.setOnClickListener {
            val email = binding.textInputEditTextEmail.text.toString().trim()
            val password = binding.textInputEditTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateLogin(email, password)) {
                android.widget.Toast.makeText(requireContext(), "Login successful!", android.widget.Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), Navbar::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                android.widget.Toast.makeText(requireContext(), "Invalid email or password", android.widget.Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }

    private fun validateLogin(email: String, password: String): Boolean {
        val prefs = requireContext().getSharedPreferences("users_prefs", android.content.Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val json = prefs.getString("users_list", null) ?: return false

        val type = object : TypeToken<MutableList<UserDetails>>() {}.type
        val users: MutableList<UserDetails> = gson.fromJson(json, type)

        // Check if email + password match any user
        return users.any { it.email == email && it.password == password }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
