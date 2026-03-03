package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentRegisterBinding
import com.example.childmonitor.network.NetworkManager
import org.json.JSONObject

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
            finish()
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
            name.length < 3 -> {
                binding.nameInput.error = "الاسم يجب أن يكون 3 أحرف على الأقل"
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
            confirmPassword.isEmpty() -> {
                binding.confirmPasswordInput.error = "الرجاء تأكيد كلمة المرور"
                binding.confirmPasswordInput.requestFocus()
                return
            }
            password != confirmPassword -> {
                binding.confirmPasswordInput.error = "كلمتا المرور غير متطابقتين"
                binding.confirmPasswordInput.requestFocus()
                return
            }
        }

        // إرسال طلب التسجيل إلى الخادم
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
                    binding.registerButton.text = "إنشاء حساب"

                    try {
                        val data = response.getJSONObject("data")
                        val parentId = data.getInt("id")
                        val parentEmail = data.getString("email")
                        val parentName = data.optString("name", "")

                        Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_LONG).show()

                        // الانتقال إلى لوحة التحكم
                        val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                            putExtra("parent_id", parentId)
                            putExtra("parent_email", parentEmail)
                            putExtra("parent_name", parentName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "خطأ في معالجة الاستجابة: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isRegistering = false
                    binding.registerButton.isEnabled = true
                    binding.registerButton.text = "إنشاء حساب"

                    if (error.contains("already registered", ignoreCase = true) || 
                        error.contains("CONFLICT", ignoreCase = true)) {
                        binding.emailInput.error = "هذا البريد الإلكتروني مسجل مسبقاً"
                        binding.emailInput.requestFocus()
                    } else {
                        Toast.makeText(this, "فشل التسجيل: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}
