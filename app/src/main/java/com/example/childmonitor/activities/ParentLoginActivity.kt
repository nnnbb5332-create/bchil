package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.database.DatabaseHelper
import com.example.childmonitor.databinding.ActivityParentLoginBinding

class ParentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, ParentRegisterActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun attemptLogin() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        // التحقق من صحة البيانات
        when {
            email.isEmpty() -> {
                binding.emailInput.error = "الرجاء إدخال البريد الإلكتروني"
                binding.emailInput.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailInput.error = "البريد الإلكتروني غير صالح"
                binding.emailInput.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "الرجاء إدخال كلمة المرور"
                binding.passwordInput.requestFocus()
                return
            }
        }

        // محاولة تسجيل الدخول
        val parent = dbHelper.loginParent(email, password)
        
        if (parent != null) {
            Toast.makeText(this, "مرحباً ${parent.name}!", Toast.LENGTH_SHORT).show()
            
            // حفظ بيانات الجلسة
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putLong("parent_id", parent.id)
                putString("parent_email", parent.email)
                putString("parent_name", parent.name)
                putString("user_type", "parent")
                apply()
            }
            
            // الانتقال إلى لوحة التحكم
            val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                putExtra("parent_id", parent.id)
                putExtra("parent_email", parent.email)
                putExtra("parent_name", parent.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show()
        }
    }
}
