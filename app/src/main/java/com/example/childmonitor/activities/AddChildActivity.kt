package com.example.childmonitor.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityAddChildBinding

class AddChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddChildBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addButton.setOnClickListener {
            // إضافة طفل جديد
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
