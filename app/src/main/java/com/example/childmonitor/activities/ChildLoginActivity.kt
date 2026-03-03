package com.example.childmonitor.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.network.NetworkManager
import com.example.childmonitor.services.MonitoringService

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
        val code = binding.codeInput.text.toString().trim().uppercase()

        // التحقق من صحة الرمز
        when {
            code.isEmpty() -> {
                binding.codeInput.error = "الرجاء إدخال الرمز"
                binding.codeInput.requestFocus()
                return
            }
            code.length != 6 -> {
                binding.codeInput.error = "الرمز يجب أن يكون 6 أحرف"
                binding.codeInput.requestFocus()
                return
            }
        }

        // تحليل الرمز: أول رقم = معرف الطفل، بقية الأرقام = كلمة المرور
        // مثال: "001234" = معرف الطفل 1، كلمة المرور 1234
        val childIdStr = code.substring(0, 2).toIntOrNull()
        val password = code.substring(2)

        if (childIdStr == null || childIdStr == 0) {
            Toast.makeText(this, "الرمز غير صحيح", Toast.LENGTH_LONG).show()
            binding.codeInput.text?.clear()
            binding.codeInput.requestFocus()
            return
        }

        val childId = childIdStr

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
                        val data = response.getJSONObject("data")
                        val childName = data.getString("name")

                        Toast.makeText(this, "مرحباً $childName!", Toast.LENGTH_SHORT).show()

                        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putInt("child_id", childId)
                            putString("child_name", childName)
                            putString("user_type", "child")
                            apply()
                        }

                        startMonitoringService(childId)

                        Toast.makeText(this, "تم تسجيل الدخول بنجاح!\nجاري تشغيل خدمة المراقبة...", Toast.LENGTH_LONG).show()

                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "خطأ في معالجة الاستجابة: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isLoggingIn = false
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "دخول"

                    if (error.contains("Invalid", ignoreCase = true) || 
                        error.contains("UNAUTHORIZED", ignoreCase = true)) {
                        Toast.makeText(this, "الرمز غير صحيح أو غير موجود", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "فشل تسجيل الدخول: $error", Toast.LENGTH_LONG).show()
                    }
                    binding.codeInput.text?.clear()
                    binding.codeInput.requestFocus()
                }
            }
        )
    }

    private fun startMonitoringService(childId: Int) {
        val intent = Intent(this, MonitoringService::class.java).apply {
            putExtra("child_id", childId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
