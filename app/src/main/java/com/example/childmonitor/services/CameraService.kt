package com.example.childmonitor.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.childmonitor.R
import com.example.childmonitor.network.NetworkManager
import java.io.ByteArrayOutputStream
import java.util.*

class CameraService : Service() {

    private var childId: Int = -1
    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private val networkManager = NetworkManager.getInstance()
    private var timer: Timer? = null

    companion object {
        const val CHANNEL_ID = "camera_channel"
        const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        childId = intent?.getIntExtra("child_id", -1) ?: -1

        if (childId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        
        // التحقق من وجود طلبات التقاط صورة كل 10 ثوانٍ
        startCheckingForRequests()

        return START_STICKY
    }

    private fun startCheckingForRequests() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkForCameraRequest()
            }
        }, 5000, 10000)
    }

    private fun checkForCameraRequest() {
        // هنا يمكن إضافة منطق للتحقق من وجود طلب من الخادم
        // في هذا المثال، سنقوم بالتقاط صورة عند الطلب عبر NetworkManager
        // (يفضل استخدام Firebase Cloud Messaging للطلبات الفورية)
    }

    private fun takePhoto() {
        try {
            val cameraId = cameraManager?.cameraIdList?.get(0) ?: return
            
            cameraManager?.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, Handler(Looper.getMainLooper()))
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createCaptureSession() {
        imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            image.close()
            
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            sendImageToServer(bitmap)
            
            cameraDevice?.close()
            cameraDevice = null
        }, Handler(Looper.getMainLooper()))

        val surface = imageReader?.surface ?: return
        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                builder?.addTarget(surface)
                session.capture(builder!!.build(), null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, Handler(Looper.getMainLooper()))
    }

    private fun sendImageToServer(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        networkManager.sendCameraImage(
            childId = childId,
            imageBase64 = imageBase64,
            onSuccess = { Log.d("CameraService", "Image sent successfully") },
            onError = { error -> Log.e("CameraService", "Failed to send image: $error") }
        )
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("مراقبة الكاميرا")
        .setContentText("جاري العمل في الخلفية...")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "مراقبة الكاميرا",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        cameraDevice?.close()
    }
}
