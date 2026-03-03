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

        val childPassword = String.format("%04d", Random.nextInt(10000))

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
                    binding.addButton.text = "إضافة"

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
                    binding.addButton.text = "إضافة"
                    Toast.makeText(this, "فشل إضافة الطفل: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showChildCodeDialog(childName: String, childPassword: String, childId: Int) {
        val message = """
            تم إضافة $childName بنجاح!
            
            معرف الطفل: $childId
            رمز الدخول: $childPassword
            
            احفظ هذه البيانات جيداً!
            سيقوم الطفل باستخدامها للدخول إلى التطبيق.
        """.trimIndent()

        val dialog = AlertDialog.Builder(this)
            .setTitle("تم إضافة الطفل")
            .setMessage(message)
            .setPositiveButton("نسخ البيانات") { _, _ ->
                val text = "معرف الطفل: $childId\nرمز الدخول: $childPassword"
                copyToClipboard(text)
                Toast.makeText(this, "تم نسخ البيانات", Toast.LENGTH_SHORT).show()
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
        val clip = ClipData.newPlainText("بيانات الطفل", text)
        clipboard.setPrimaryClip(clip)
    }
}
