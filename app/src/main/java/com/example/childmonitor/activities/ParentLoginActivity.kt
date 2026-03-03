package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentLoginBinding
import com.example.childmonitor.network.NetworkManager

class ParentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentLoginBinding
    private val networkManager = NetworkManager.getInstance()
    private var isLoggingIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            if (!isLoggingIn) {
                attemptLogin()
            }
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

        // إرسال طلب الدخول إلى الخادم
        isLoggingIn = true
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "جاري الدخول..."

        networkManager.loginParent(
            email = email,
            password = password,
            onSuccess = { response ->
                runOnUiThread {
                    isLoggingIn = false
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "دخول"

                    try {
                        Log.d("ParentLogin", "Response: $response")
                        
                        // ✅ الاستجابة هي البيانات مباشرة
                        val parentId = response.getInt("id")
                        val parentEmail = response.getString("email")
                        val parentName = response.optString("name", "")

                        Toast.makeText(this, "تم تسجيل الدخول بنجاح!", Toast.LENGTH_LONG).show()

                        // ✅ حفظ بيانات الجلسة بشكل صحيح
                        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putInt("parent_id", parentId)
                            putString("parent_email", parentEmail)
                            putString("parent_name", parentName)
                            putString("user_type", "parent")
                            putBoolean("is_logged_in", true) // ✅ مهم جداً!
                            apply()
                        }

                        Log.d("ParentLogin", "Saved session - ID: $parentId, Email: $parentEmail")

                        // ✅ الانتقال إلى لوحة التحكم
                        val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                            putExtra("parent_id", parentId)
                            putExtra("parent_email", parentEmail)
                            putExtra("parent_name", parentName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("ParentLogin", "Error parsing response", e)
                        Toast.makeText(this, "خطأ في معالجة الاستجابة: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isLoggingIn = false
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "دخول"

                    when {
                        error.contains("Invalid", ignoreCase = true) || 
                        error.contains("UNAUTHORIZED", ignoreCase = true) ||
                        error.contains("كلمة المرور", ignoreCase = true) -> {
                            Toast.makeText(this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show()
                        }
                        error.contains("not found", ignoreCase = true) -> {
                            Toast.makeText(this, "هذا البريد الإلكتروني غير مسجل", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "فشل تسجيل الدخول: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}
