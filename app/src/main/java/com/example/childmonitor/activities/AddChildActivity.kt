package com.example.childmonitor.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityAddChildBinding
import com.example.childmonitor.network.NetworkManager
import kotlin.random.Random

class AddChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddChildBinding
    private val networkManager = NetworkManager.getInstance()
    private var parentId: Int = -1
    private var isAdding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        parentId = sharedPref.getInt("parent_id", -1)
        
        if (parentId == -1) {
            Toast.makeText(this, "خطأ: لم يتم تسجيل الدخول", Toast.LENGTH_LONG).show()
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
        val childName = binding.childNameInput.text.toString().trim()

        when {
            childName.isEmpty() -> {
                binding.childNameInput.error = "الرجاء إدخال اسم الطفل"
                binding.childNameInput.requestFocus()
                return
            }
            childName.length < 2 -> {
                binding.childNameInput.error = "اسم الطفل يجب أن يكون حرفين على الأقل"
                binding.childNameInput.requestFocus()
                return
            }
        }

        // إنشاء رمز عشوائي (6 أحرف وأرقام)
        val childPassword = generateRandomCode()

        isAdding = true
        binding.addButton.isEnabled = false
        binding.addButton.text = "جاري الإضافة..."

        networkManager.addChild(
            parentId = parentId,
            name = childName,
            password = childPassword,
            onSuccess = { response ->
                runOnUiThread {
                    isAdding = false
                    binding.addButton.isEnabled = true
                    binding.addButton.text = "إضافة الطفل"

                    try {
                        val data = response.getJSONObject("data")
                        val childId = data.getInt("id")
                        showChildCodeDialog(childName, childPassword, childId)
                    } catch (e: Exception) {
                        Toast.makeText(this, "خطأ في معالجة الاستجابة: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    isAdding = false
                    binding.addButton.isEnabled = true
                    binding.addButton.text = "إضافة الطفل"
                    Toast.makeText(this, "فشل إضافة الطفل: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun generateRandomCode(): String {
        // إنشاء رمز من 6 أحرف وأرقام
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun showChildCodeDialog(childName: String, childPassword: String, childId: Int) {
        // تحويل معرف الطفل إلى صيغة الرمز (مثال: 5 يصبح 05)
        val childIdFormatted = String.format("%02d", childId)
        val fullCode = childIdFormatted + childPassword

        val message = """
            تم إضافة $childName بنجاح!
            
            رمز الدخول هو:
            $fullCode
            
            احفظ هذا الرمز جيداً!
            سيقوم الطفل باستخدامه للدخول إلى التطبيق.
            
            الرمز يتكون من:
            - أول رقمين: معرف الطفل
            - باقي الأحرف: كلمة المرور
        """.trimIndent()

        val dialog = AlertDialog.Builder(this)
            .setTitle("تم إضافة الطفل")
            .setMessage(message)
            .setPositiveButton("نسخ الرمز") { _, _ ->
                copyToClipboard(fullCode)
                Toast.makeText(this, "تم نسخ الرمز", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("إغلاق") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("رمز الطفل", text)
        clipboard.setPrimaryClip(clip)
    }
}
