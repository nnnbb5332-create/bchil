package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.childmonitor.database.DatabaseHelper
import com.example.childmonitor.databinding.ActivityParentDashboardBinding
import com.google.android.material.card.MaterialCardView

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var childrenAdapter: ChildrenAdapter
    private var parentId: Long = -1
    private var parentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        // الحصول على بيانات ولي الأمر
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        parentId = sharedPref.getLong("parent_id", -1)
        parentName = sharedPref.getString("parent_name", "") ?: ""
        
        if (parentId == -1L) {
            Toast.makeText(this, "خطأ: لم يتم تسجيل الدخول", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // عرض اسم ولي الأمر
        binding.welcomeText.text = "مرحباً، $parentName"

        // إعداد RecyclerView
        setupRecyclerView()

        // تحميل قائمة الأطفال
        loadChildren()

        binding.addChildButton.setOnClickListener {
            startActivity(Intent(this, AddChildActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // إعادة تحميل قائمة الأطفال عند العودة
        loadChildren()
    }

    private fun setupRecyclerView() {
        childrenAdapter = ChildrenAdapter(
            onChildClick = { child ->
                showChildOptionsDialog(child)
            }
        )
        binding.childrenRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ParentDashboardActivity)
            adapter = childrenAdapter
        }
    }

    private fun loadChildren() {
        val children = dbHelper.getChildrenByParent(parentId)
        
        if (children.isEmpty()) {
            binding.childrenRecyclerView.visibility = View.GONE
            binding.noChildrenText.visibility = View.VISIBLE
        } else {
            binding.childrenRecyclerView.visibility = View.VISIBLE
            binding.noChildrenText.visibility = View.GONE
            childrenAdapter.submitList(children)
        }
        
        // تحديث عدد الأطفال
        binding.childrenCountText.text = "عدد الأطفال: ${children.size}"
    }

    private fun showChildOptionsDialog(child: DatabaseHelper.Child) {
        val options = arrayOf("عرض الرمز", "عرض المواقع", "حذف الطفل")
        
        AlertDialog.Builder(this)
            .setTitle(child.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showChildCodeDialog(child)
                    1 -> showChildLocations(child)
                    2 -> showDeleteConfirmDialog(child)
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showChildCodeDialog(child: DatabaseHelper.Child) {
        AlertDialog.Builder(this)
            .setTitle("رمز دخول ${child.name}")
            .setMessage("""
                |الرمز:
                |${child.code}
                |
                |أعطِ هذا الرمز للطفل ليتمكن من الدخول.
            """.trimMargin())
            .setPositiveButton("نسخ") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("رمز الطفل", child.code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "تم نسخ الرمز", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun showChildLocations(child: DatabaseHelper.Child) {
        val locations = dbHelper.getChildLocations(child.id, 10)
        
        if (locations.isEmpty()) {
            Toast.makeText(this, "لا توجد مواقع مسجلة لهذا الطفل", Toast.LENGTH_LONG).show()
            return
        }

        val message = StringBuilder()
        message.append("آخر المواقع لـ ${child.name}:\n\n")
        
        locations.forEachIndexed { index, location ->
            message.append("${index + 1}. ${location.address ?: "موقع غير معروف"}\n")
            message.append("   خط العرض: ${location.latitude}\n")
            message.append("   خط الطول: ${location.longitude}\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("مواقع ${child.name}")
            .setMessage(message.toString())
            .setPositiveButton("إغلاق", null)
            .show()
    }

    private fun showDeleteConfirmDialog(child: DatabaseHelper.Child) {
        AlertDialog.Builder(this)
            .setTitle("حذف ${child.name}")
            .setMessage("هل أنت متأكد من حذف هذا الطفل؟\nسيتم حذف جميع بياناته بشكل نهائي.")
            .setPositiveButton("حذف") { _, _ ->
                if (dbHelper.deleteChild(child.id)) {
                    Toast.makeText(this, "تم حذف ${child.name}", Toast.LENGTH_SHORT).show()
                    loadChildren()
                } else {
                    Toast.makeText(this, "فشل الحذف", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("هل أنت متأكد من تسجيل الخروج؟")
            .setPositiveButton("تسجيل الخروج") { _, _ ->
                logout()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun logout() {
        // مسح بيانات الجلسة
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        
        // الانتقال إلى الشاشة الرئيسية
        val intent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // ==================== Children Adapter ====================
    
    inner class ChildrenAdapter(
        private val onChildClick: (DatabaseHelper.Child) -> Unit
    ) : RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder>() {

        private var children: List<DatabaseHelper.Child> = emptyList()

        fun submitList(newList: List<DatabaseHelper.Child>) {
            children = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ChildViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
            holder.bind(children[position])
        }

        override fun getItemCount(): Int = children.size

        inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameText: TextView = itemView.findViewById(android.R.id.text1)
            private val codeText: TextView = itemView.findViewById(android.R.id.text2)

            fun bind(child: DatabaseHelper.Child) {
                nameText.text = child.name
                codeText.text = "الرمز: ${child.code}"
                
                itemView.setOnClickListener {
                    onChildClick(child)
                }
            }
        }
    }
}
