package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.database.DatabaseHelper
import com.example.childmonitor.databinding.ActivityParentRegisterBinding

class ParentRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.registerButton.setOnClickListener {
            attemptRegister()
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

        // التحقق من عدم وجود البريد مسبقاً
        if (dbHelper.isEmailExists(email)) {
            binding.emailInput.error = "هذا البريد الإلكتروني مسجل مسبقاً"
            binding.emailInput.requestFocus()
            return
        }

        // إنشاء الحساب
        val parentId = dbHelper.registerParent(email, password, name)
        
        if (parentId != -1L) {
            Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_LONG).show()
            
            // الانتقال إلى لوحة التحكم
            val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                putExtra("parent_id", parentId)
                putExtra("parent_email", email)
                putExtra("parent_name", name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "فشل إنشاء الحساب، حاول مرة أخرى", Toast.LENGTH_LONG).show()
        }
    }
}
