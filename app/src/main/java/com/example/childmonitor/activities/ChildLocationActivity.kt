package com.example.childmonitor.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLocationBinding
import com.example.childmonitor.network.NetworkManager
import org.json.JSONArray

class ChildLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildLocationBinding
    private val networkManager = NetworkManager.getInstance()
    private var childId: Int = -1
    private var childName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        childId = intent.getIntExtra("child_id", -1)
        childName = intent.getStringExtra("child_name") ?: ""

        if (childId == -1) {
            Toast.makeText(this, "خطأ: لم يتم تحديد الطفل", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        title = "موقع $childName"

        binding.childNameText.text = "الطفل: $childName"

        binding.refreshButton.setOnClickListener {
            loadLocation()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        loadLocation()
    }

    private fun loadLocation() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.locationText.text = "جاري التحميل..."

        networkManager.getLatestLocation(
            childId = childId,
            onSuccess = { location ->
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    try {
                        val latitude = location.getDouble("latitude")
                        val longitude = location.getDouble("longitude")
                        val address = location.optString("address", "")
                        val timestamp = location.optString("timestamp", "")

                        binding.locationText.text = buildString {
                            append("الموقع الحالي:\n")
                            append("خط العرض: $latitude\n")
                            append("خط الطول: $longitude\n")
                            if (address.isNotEmpty()) {
                                append("العنوان: $address\n")
                            }
                            if (timestamp.isNotEmpty()) {
                                append("الوقت: $timestamp")
                            }
                        }

                        // يمكنك هنا إضافة خريطة باستخدام Google Maps
                        
                    } catch (e: Exception) {
                        Log.e("ChildLocation", "Error parsing location", e)
                        binding.locationText.text = "خطأ في عرض الموقع"
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.locationText.text = "لا يوجد موقع متاح\n$error"
                }
            }
        )
    }
}
