package com.example.childmonitor.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.database.DatabaseHelper
import com.example.childmonitor.databinding.ActivityAddChildBinding

class AddChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddChildBinding
    private lateinit var dbHelper: DatabaseHelper
    private var parentId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        // الحصول على معرف ولي الأمر
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        parentId = sharedPref.getLong("parent_id", -1)
        
        if (parentId == -1L) {
            Toast.makeText(this, "خطأ: لم يتم تسجيل الدخول", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.addButton.setOnClickListener {
            attemptAddChild()
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

        // إضافة الطفل وإنشاء الرمز
        val childCode = dbHelper.addChild(parentId, childName)
        
        if (childCode != null) {
            showChildCodeDialog(childName, childCode)
        } else {
            Toast.makeText(this, "فشل إضافة الطفل، حاول مرة أخرى", Toast.LENGTH_LONG).show()
        }
    }

    private fun showChildCodeDialog(childName: String, childCode: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("تم إضافة $childName بنجاح!")
            .setMessage("""
                |رمز دخول الطفل هو:
                |
                |$childCode
                |
                |احفظ هذا الرمز جيداً! 
                |سيقوم الطفل باستخدامه للدخول إلى التطبيق.
                |
                |يمكنك نسخ الرمز والمشاركة مع الطفل.
            """.trimMargin())
            .setPositiveButton("نسخ الرمز") { _, _ ->
                copyToClipboard(childCode)
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
