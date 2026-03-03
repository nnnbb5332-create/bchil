package com.example.childmonitor.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityAddChildBinding
import com.example.childmonitor.network.NetworkManager

class AddChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddChildBinding
    private val networkManager = NetworkManager.getInstance()
    private var parentId: Int = -1
    private var isAdding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // قراءة parentId
        parentId = intent.getIntExtra("parent_id", -1)

        if (parentId == -1) {
            // محاولة القراءة من SharedPreferences
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            parentId = sharedPref.getInt("parent_id", -1)
        }

        if (parentId == -1) {
            Toast.makeText(this, "خطأ: لم يتم تحديد الأب", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.addButton.setOnClickListener {
            if (!isAdding) {
                attemptAddChild()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun attemptAddChild() {
        val name = binding.nameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        // التحقق من صحة البيانات
        when {
            name.isEmpty() -> {
                binding.nameInput.error = "الرجاء إدخال اسم الطفل"
                binding.nameInput.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "الرجاء إدخال كلمة المرور"
                binding.passwordInput.requestFocus()
                return
            }
            password.length < 4 -> {
                binding.passwordInput.error = "كلمة المرور يجب أن تكون 4 أحرف على الأقل"
                binding.passwordInput.requestFocus()
                return
            }
            password != confirmPassword -> {
                binding.confirmPasswordInput.error = "كلمتا المرور غير متطابقتين"
                binding.confirmPasswordInput.requestFocus()
                return
            }
        }

        // إرسال طلب الإضافة
        isAdding = true
        binding.addButton.isEnabled = false
        binding.addButton.text = "جاري الإضافة..."

        networkManager.addChild(
            parentId = parentId,
            name = name,
            password = password,
            onSuccess = { response ->
                runOnUiThread {
                    isAdding = false
                    binding.addButton.isEnabled = true
                    binding.addButton.text = "إضافة"

                    try {
                        Log.d("AddChild", "Response: $response")
                        
                        val childId = response.getInt("id")
                        val childCode = response.optString("code", childId.toString())

                        Toast.makeText(this, "تم إضافة الطفل بنجاح!", Toast.LENGTH_LONG).show()

                        // عرض الرمز للمستخدم
                        binding.resultText.text = "رمز الطفل: $childCode"
                        binding.resultText.visibility = android.view.View.VISIBLE

                        // مسح الحقول
                        binding.nameInput.text?.clear()
                        binding.passwordInput.text?.clear()
                        binding.confirmPasswordInput.text?.clear()

                    } catch (e: Exception) {
                        Log.e("AddChild", "Error parsing response", e)
                        Toast.makeText(this, "تم الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isAdding = false
                    binding.addButton.isEnabled = true
                    binding.addButton.text = "إضافة"

                    Toast.makeText(this, "فشل الإضافة: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
