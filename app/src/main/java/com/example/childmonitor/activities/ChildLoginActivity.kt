package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.network.NetworkManager

class ChildLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildLoginBinding
    private val networkManager = NetworkManager.getInstance()
    private var isLoggingIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            if (!isLoggingIn) {
                attemptLogin()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun attemptLogin() {
        val childIdText = binding.childIdInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        // التحقق من صحة البيانات
        when {
            childIdText.isEmpty() -> {
                binding.childIdInput.error = "الرجاء إدخال رمز الطفل"
                binding.childIdInput.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "الرجاء إدخال كلمة المرور"
                binding.passwordInput.requestFocus()
                return
            }
        }

        val childId = childIdText.toIntOrNull()
        if (childId == null) {
            binding.childIdInput.error = "رمز الطفل يجب أن يكون رقماً"
            binding.childIdInput.requestFocus()
            return
        }

        // إرسال طلب الدخول
        isLoggingIn = true
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "جاري الدخول..."

        networkManager.loginChild(
            childId = childId,
            password = password,
            onSuccess = { response ->
                runOnUiThread {
                    isLoggingIn = false
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "دخول"

                    try {
                        Log.d("ChildLogin", "Response: $response")
                        
                        // ✅ الاستجابة هي البيانات مباشرة
                        val returnedChildId = response.getInt("id")
                        val childName = response.optString("name", "")
                        val parentId = response.getInt("parentId")

                        Toast.makeText(this, "تم تسجيل الدخول بنجاح!", Toast.LENGTH_LONG).show()

                        // ✅ حفظ بيانات الجلسة
                        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putInt("child_id", returnedChildId)
                            putString("child_name", childName)
                            putInt("parent_id", parentId)
                            putString("user_type", "child")
                            putBoolean("is_logged_in", true)
                            apply()
                        }

                        Log.d("ChildLogin", "Saved session - Child ID: $returnedChildId")

                        // ✅ الانتقال إلى لوحة تحكم الطفل
                        val intent = Intent(this, ChildDashboardActivity::class.java).apply {
                            putExtra("child_id", returnedChildId)
                            putExtra("child_name", childName)
                            putExtra("parent_id", parentId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("ChildLogin", "Error parsing response", e)
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
                        error.contains("UNAUTHORIZED", ignoreCase = true) -> {
                            Toast.makeText(this, "رمز الطفل أو كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show()
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
