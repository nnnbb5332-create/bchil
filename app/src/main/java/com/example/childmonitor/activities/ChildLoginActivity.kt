package com.example.childmonitor.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.database.DatabaseHelper
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.services.MonitoringService

class ChildLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.loginButton.setOnClickListener {
            attemptLogin()
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

        // محاولة تسجيل الدخول بالرمز
        val child = dbHelper.loginChild(code)
        
        if (child != null) {
            Toast.makeText(this, "مرحباً ${child.name}!", Toast.LENGTH_SHORT).show()
            
            // حفظ بيانات الجلسة
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putLong("child_id", child.id)
                putLong("parent_id", child.parentId)
                putString("child_name", child.name)
                putString("child_code", child.code)
                putString("user_type", "child")
                apply()
            }
            
            // بدء خدمة المراقبة
            startMonitoringService(child.id)
            
            // إظهار رسالة نجاح
            Toast.makeText(this, "تم تسجيل الدخول بنجاح!\nجاري تشغيل خدمة المراقبة...", Toast.LENGTH_LONG).show()
            
            // إغلاق النشاط (الطفل لا يرى واجهة بعد الدخول)
            finish()
        } else {
            Toast.makeText(this, "الرمز غير صحيح أو غير موجود", Toast.LENGTH_LONG).show()
            binding.codeInput.text?.clear()
            binding.codeInput.requestFocus()
        }
    }

    private fun startMonitoringService(childId: Long) {
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
