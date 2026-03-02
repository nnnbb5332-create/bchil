package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentLoginBinding
import com.example.childmonitor.api.ApiClient

class ParentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentLoginBinding
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginParent(email, password)
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loginParent(email: String, password: String) {
        apiClient.loginParent(
            email,
            password,
            onSuccess = { response ->
                try {
                    val parentId = response.substringAfter("\"parentId\":\"").substringBefore("\"")

                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().apply {
                        putString("parent_id", parentId)
                        putString("parent_email", email)
                        putString("user_type", "parent")
                        apply()
                    }

                    startActivity(Intent(this, ParentDashboardActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "خطأ في معالجة البيانات", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
