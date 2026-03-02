package com.example.childmonitor.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.api.ApiClient
import com.example.childmonitor.services.MonitoringService

class ChildLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildLoginBinding
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val password = binding.passwordInput.text.toString()

            if (password.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال كلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginChild(password)
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loginChild(password: String) {
        apiClient.loginChild(
            password,
            onSuccess = { response ->
                try {
                    val childId = response.substringAfter("\"childId\":\"").substringBefore("\"")

                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().apply {
                        putString("child_id", childId)
                        putString("user_type", "child")
                        apply()
                    }

                    // بدء خدمة المراقبة
                    val intent = Intent(this, MonitoringService::class.java)
                    intent.putExtra("child_id", childId)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }

                    Toast.makeText(this, "تم بدء المراقبة", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "خطأ في معالجة البيانات", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
