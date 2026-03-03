package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentRegisterBinding
import com.example.childmonitor.network.NetworkManager

class ParentRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentRegisterBinding
    private val networkManager = NetworkManager.getInstance()
    private var isRegistering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            if (!isRegistering) {
                attemptRegister()
            }
        }

        binding.loginLink.setOnClickListener {
            finish() // العودة لشاشة تسجيل الدخول
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegister() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        // التحقق من صحة البيانات
        when {
            name.isEmpty() -> {
                binding.nameInput.error = "الرجاء إدخال الاسم"
                binding.nameInput.requestFocus()
                return
            }
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
            password.length < 6 -> {
                binding.passwordInput.error = "كلمة المرور يجب أن تكون 6 أحرف على الأقل"
                binding.passwordInput.requestFocus()
                return
            }
            password != confirmPassword -> {
                binding.confirmPasswordInput.error = "كلمتا المرور غير متطابقتين"
                binding.confirmPasswordInput.requestFocus()
                return
            }
        }

        // إرسال طلب التسجيل
        isRegistering = true
        binding.registerButton.isEnabled = false
        binding.registerButton.text = "جاري التسجيل..."

        networkManager.registerParent(
            email = email,
            password = password,
            name = name,
            onSuccess = { response ->
                runOnUiThread {
                    isRegistering = false
                    binding.registerButton.isEnabled = true
                    binding.registerButton.text = "تسجيل"

                    try {
                        Log.d("ParentRegister", "Response: $response")
                        
                        // ✅ الاستجابة هي البيانات مباشرة
                        val parentId = response.getInt("id")
                        val parentEmail = response.getString("email")
                        val parentName = response.optString("name", "")

                        Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_LONG).show()

                        // ✅ حفظ بيانات الجلسة
                        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putInt("parent_id", parentId)
                            putString("parent_email", parentEmail)
                            putString("parent_name", parentName)
                            putString("user_type", "parent")
                            putBoolean("is_logged_in", true)
                            apply()
                        }

                        Log.d("ParentRegister", "Saved session - ID: $parentId")

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
                        Log.e("ParentRegister", "Error parsing response", e)
                        Toast.makeText(this, "خطأ في معالجة الاستجابة: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isRegistering = false
                    binding.registerButton.isEnabled = true
                    binding.registerButton.text = "تسجيل"

                    when {
                        error.contains("already exists", ignoreCase = true) || 
                        error.contains("مستخدم", ignoreCase = true) -> {
                            Toast.makeText(this, "هذا البريد الإلكتروني مسجل مسبقاً", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "فشل التسجيل: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}
