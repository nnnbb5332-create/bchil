package com.example.childmonitor.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildCameraBinding
import com.example.childmonitor.network.NetworkManager
import java.util.*

class ChildCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildCameraBinding
    private val networkManager = NetworkManager.getInstance()
    private var childId: Int = -1
    private var childName: String = ""
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        childId = intent.getIntExtra("child_id", -1)
        childName = intent.getStringExtra("child_name") ?: "الطفل"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "كاميرا $childName"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.captureButton.setOnClickListener {
            requestNewImage()
        }

        // جلب آخر صورة عند الفتح
        fetchLatestImage()
        
        // تحديث تلقائي كل 10 ثوانٍ
        startAutoUpdate()
    }

    private fun requestNewImage() {
        if (childId == -1) return

        binding.progressBar.visibility = View.VISIBLE
        binding.statusText.text = "جاري إرسال طلب التقاط صورة..."

        networkManager.requestCameraImage(
            childId = childId,
            onSuccess = {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.text = "تم إرسال الطلب، بانتظار الصورة..."
                    Toast.makeText(this, "تم إرسال طلب التقاط صورة", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.text = "فشل إرسال الطلب: $error"
                    Toast.makeText(this, "فشل الطلب: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun fetchLatestImage() {
        if (childId == -1) return

        networkManager.getLatestCameraImage(
            childId = childId,
            onSuccess = { response ->
                runOnUiThread {
                    try {
                        val imageBase64 = response.optString("image", "")
                        if (imageBase64.isNotEmpty()) {
                            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.cameraImageView.setImageBitmap(bitmap)
                            binding.statusText.text = "آخر صورة ملتقطة"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onError = { /* تجاهل الخطأ في التحديث التلقائي */ }
        )
    }

    private fun startAutoUpdate() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                fetchLatestImage()
            }
        }, 5000, 10000) // كل 10 ثوانٍ
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
