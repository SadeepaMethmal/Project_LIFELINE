package com.example.lifeline

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.lifeline.databinding.FragmentProfileBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        val prefs = requireContext().getSharedPreferences("users_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("users_list", null)

        val userName = view.findViewById<TextView>(R.id.profileName)
        val email = view.findViewById<TextView>(R.id.email)

        if (json != null) {
            val type = object : TypeToken<MutableList<UserDetails>>() {}.type
            val users: MutableList<UserDetails> = Gson().fromJson(json, type)

            if (users.isNotEmpty()) {
                val currentUser = users.last()  // get the last registered user
                userName.text = currentUser.name
                email.text = currentUser.email
            } else {
                userName.text = "Profile Name"
                email.text = "email"
            }
        } else {
            userName.text = "Profile Name"
            email.text = "email"
        }

        binding.logoutBtn.setOnClickListener {
            showResetConfirmDialog()
        }


    }

    private fun showResetConfirmDialog() {
        val confirmView = layoutInflater.inflate(R.layout.custom_confirm_box3, null)
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(confirmView)
            .create()

        val btnYes = confirmView.findViewById<Button>(R.id.btnResetYes)
        val btnNo = confirmView.findViewById<Button>(R.id.btnResetNo)

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnYes.setOnClickListener {
            val intent = Intent(requireContext(), Page5::class.java)
            startActivity(intent)
        }

        btnNo.setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.show()

        val window = confirmDialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),  // 80% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
