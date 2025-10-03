package com.example.lifeline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
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
    }
}
