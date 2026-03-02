package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.childmonitor.databinding.ActivityParentDashboardBinding
import com.example.childmonitor.api.ApiClient

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadChildren()

        binding.addChildButton.setOnClickListener {
            startActivity(Intent(this, AddChildActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.childrenRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadChildren() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val parentId = sharedPref.getString("parent_id", "") ?: ""

        apiClient.getChildren(
            parentId,
            onSuccess = { response ->
                // معالجة البيانات وعرضها
            },
            onError = {
                // معالجة الخطأ
            }
        )
    }
}
