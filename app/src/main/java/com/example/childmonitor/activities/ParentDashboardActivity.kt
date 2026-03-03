package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.childmonitor.databinding.ActivityParentDashboardBinding
import com.example.childmonitor.network.NetworkManager
import org.json.JSONArray
import org.json.JSONObject

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private val networkManager = NetworkManager.getInstance()
    private var parentId: Int = -1
    private var parentEmail: String = ""
    private var parentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ قراءة البيانات من Intent أو SharedPreferences
        parentId = intent.getIntExtra("parent_id", -1)
        parentEmail = intent.getStringExtra("parent_email") ?: ""
        parentName = intent.getStringExtra("parent_name") ?: ""

        // إذا لم تكن البيانات في Intent، اقرأها من SharedPreferences
        if (parentId == -1) {
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            parentId = sharedPref.getInt("parent_id", -1)
            parentEmail = sharedPref.getString("parent_email", "") ?: ""
            parentName = sharedPref.getString("parent_name", "") ?: ""
        }

        // ✅ التحقق من تسجيل الدخول
        if (parentId == -1) {
            Toast.makeText(this, "يرجى تسجيل الدخول مرة أخرى", Toast.LENGTH_LONG).show()
            logout()
            return
        }

        Log.d("ParentDashboard", "Parent ID: $parentId, Email: $parentEmail")

        // عرض معلومات الأب
        binding.welcomeText.text = "مرحباً، ${if (parentName.isNotEmpty()) parentName else parentEmail}"

        // زر إضافة طفل
        binding.addChildButton.setOnClickListener {
            val intent = Intent(this, AddChildActivity::class.java)
            intent.putExtra("parent_id", parentId)
            startActivity(intent)
        }

        // زر تحديث القائمة
        binding.refreshButton.setOnClickListener {
            loadChildren()
        }

        // زر تسجيل الخروج
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    override fun onResume() {
        super.onResume()
        loadChildren()
    }

    private fun loadChildren() {
        if (parentId == -1) return

        binding.swipeRefresh.isRefreshing = true

        networkManager.getChildren(
            parentId = parentId,
            onSuccess = { childrenArray ->
                runOnUiThread {
                    binding.swipeRefresh.isRefreshing = false
                    displayChildren(childrenArray)
                }
            },
            onError = { error ->
                runOnUiThread {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, "فشل تحميل الأطفال: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun displayChildren(childrenArray: JSONArray) {
        binding.childrenContainer.removeAllViews()

        if (childrenArray.length() == 0) {
            binding.emptyStateText.text = "لا يوجد أطفال مسجلين. أضف طفلاً جديداً!"
            binding.emptyStateText.visibility = android.view.View.VISIBLE
            return
        }

        binding.emptyStateText.visibility = android.view.View.GONE

        for (i in 0 until childrenArray.length()) {
            val child = childrenArray.getJSONObject(i)
            val childId = child.getInt("id")
            val childName = child.optString("name", "طفل $childId")
            val childCode = child.optString("code", "")

            val childView = layoutInflater.inflate(
                com.example.childmonitor.R.layout.item_child,
                binding.childrenContainer,
                false
            )

            val nameText = childView.findViewById<android.widget.TextView>(com.example.childmonitor.R.id.childNameText)
            val codeText = childView.findViewById<android.widget.TextView>(com.example.childmonitor.R.id.childCodeText)
            val viewLocationButton = childView.findViewById<android.widget.Button>(com.example.childmonitor.R.id.viewLocationButton)
            val deleteButton = childView.findViewById<android.widget.Button>(com.example.childmonitor.R.id.deleteChildButton)

            nameText.text = childName
            codeText.text = "الرمز: $childCode"

            viewLocationButton.setOnClickListener {
                viewChildLocation(childId, childName)
            }

            deleteButton.setOnClickListener {
                showDeleteConfirmation(childId, childName)
            }

            binding.childrenContainer.addView(childView)
        }
    }

    private fun viewChildLocation(childId: Int, childName: String) {
        val intent = Intent(this, ChildLocationActivity::class.java)
        intent.putExtra("child_id", childId)
        intent.putExtra("child_name", childName)
        startActivity(intent)
    }

    private fun showDeleteConfirmation(childId: Int, childName: String) {
        AlertDialog.Builder(this)
            .setTitle("حذف طفل")
            .setMessage("هل أنت متأكد من حذف $childName؟")
            .setPositiveButton("حذف") { _, _ ->
                deleteChild(childId)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun deleteChild(childId: Int) {
        networkManager.deleteChild(
            childId = childId,
            onSuccess = { _ ->
                runOnUiThread {
                    Toast.makeText(this, "تم حذف الطفل بنجاح", Toast.LENGTH_SHORT).show()
                    loadChildren()
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, "فشل الحذف: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("هل أنت متأكد من تسجيل الخروج؟")
            .setPositiveButton("نعم") { _, _ ->
                logout()
            }
            .setNegativeButton("لا", null)
            .show()
    }

    private fun logout() {
        // ✅ مسح بيانات الجلسة
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Log.d("ParentDashboard", "Logged out, session cleared")

        // ✅ الانتقال لشاشة البداية
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // منع الرجوع لتجنب الخروج من التطبيق
        moveTaskToBack(true)
    }
}
