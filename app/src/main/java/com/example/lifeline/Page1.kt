package com.example.lifeline

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Page1 : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        // for the PrefManager.kt
        val prefManager = PrefManager(this)
        prefManager.clearAll()  // to clear the shared preference of onboarding screens


        // Attach splash before super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash until ViewModel is ready
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isReady.value
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_page1)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            viewModel.isReady.collectLatest { ready ->
                if (ready) {
                    delay(3000) // show Page1 content for 3 seconds

                    if (prefManager.isFirstTimeLaunch()) {
                        //go to the onboarding page
                        startActivity(Intent(this@Page1, Page2 ::class.java))
                    } else {
                        // Go to home page
                        startActivity(Intent(this@Page1, Navbar::class.java))
                    }


                    finish()
                }
            }
        }
    }
}
