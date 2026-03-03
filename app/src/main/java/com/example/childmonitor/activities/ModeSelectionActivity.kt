package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityModeSelectionBinding

class ModeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // نمط ولي الأمر
        binding.parentModeButton.setOnClickListener {
            val intent = Intent(this, ParentLoginActivity::class.java)
            startActivity(intent)
        }

        // نمط الطفل
        binding.childModeButton.setOnClickListener {
            val intent = Intent(this, ChildLoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // إغلاق التطبيق بالكامل
    }
}
