package com.example.lifeline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifeline.databinding.FragmentRegisterBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit


class Register : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createAccBtn.setOnClickListener {
            validateForm()
        }
    }

    private fun validateForm() {
        val name = binding.textInputEditTextName.text.toString().trim()
        val email = binding.textInputEditTextEmail.text.toString().trim()
        val password = binding.textInputEditTextPassword.text.toString().trim()
        val confirmPassword = binding.textInputEditTextConfirmPassword.text.toString().trim()
        val agreed = binding.checkBox2.isChecked

        // Name check
        if (name.isEmpty()) {
            binding.textInputLayoutName.error = "Name is required"
            return
        } else {
            binding.textInputLayoutName.error = null
        }

        // Email check
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Enter a valid email"
            return
        } else {
            binding.textInputLayoutEmail.error = null
        }

        // Password check
        if (password.length < 6) {
            binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
            return
        } else {
            binding.textInputLayoutPassword.error = null
        }

        // Confirm password check
        if (confirmPassword != password) {
            binding.textInputLayoutConfirmPassword.error = "Passwords do not match"
            return
        } else {
            binding.textInputLayoutConfirmPassword.error = null
        }

        // Terms & conditions
        if (!agreed) {
            Toast.makeText(requireContext(), "You must agree to the terms", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user details in a shared preference
        val user = UserDetails(name, email, password)
        saveUser(user)

        // If everything is valid
        Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to the login page
        parentFragmentManager.beginTransaction()
            .replace(R.id.viewPager, Login())
            .addToBackStack(null)
            .commit()

    }

    private fun saveUser(user: UserDetails){
        val prefs = requireContext().getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()

        // Load existing users
        val json = prefs.getString("users_list",null)
        val type = object : TypeToken<MutableList<UserDetails>>() {}.type
        val users: MutableList<UserDetails> = if (json != null) gson.fromJson(json, type) else mutableListOf()


        // Prevent duplicate emails
        if (users.any { it.email == user.email }) {
            Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
            return
        }

        // Add new user
        users.add(user)

        // Save updated list
        prefs.edit{
            putString("users_list", gson.toJson(users))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
