package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLoginButton.setOnClickListener {
            startActivity(Intent(this, ParentLoginActivity::class.java))
        }

        binding.childLoginButton.setOnClickListener {
            startActivity(Intent(this, ChildLoginActivity::class.java))
        }
    }
}
