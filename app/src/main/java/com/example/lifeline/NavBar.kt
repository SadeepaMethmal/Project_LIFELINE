package com.example.lifeline

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.lifeline.databinding.ActivityNavBarBinding
import androidx.core.view.size
import androidx.core.view.get

class Navbar : AppCompatActivity() {
    private lateinit var binding: ActivityNavBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(Home()) // fragment name
        binding.bottomNavigationView.background = null

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(Home())
                R.id.habit -> replaceFragment(Habit())
                R.id.mood -> replaceFragment(Mood())
                R.id.profile -> replaceFragment(Profile())
            }
            true
        }

        // Floating Action Button click listener
        binding.drop.setOnClickListener {
            //Deselect all nav items
            binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
            for (i in 0 until binding.bottomNavigationView.menu.size) {
                binding.bottomNavigationView.menu[i].isChecked = false
            }
            binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)

            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Hydration())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    fun navigateToFragment(fragment: Fragment, menuItemId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

        binding.bottomNavigationView.menu.findItem(menuItemId).isChecked = true
    }

}



