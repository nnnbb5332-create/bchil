package com.example.childmonitor.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.childmonitor.R
import com.example.childmonitor.activities.ChildDashboardActivity
import com.example.childmonitor.network.NetworkManager
import java.util.Locale

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var networkManager: NetworkManager
    private var childId: Int = -1
    private var parentId: Int = -1

    companion object {
        private const val CHANNEL_ID = "location_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MIN_TIME_MS = 60000L // دقيقة واحدة
        private const val MIN_DISTANCE_M = 50f // 50 متر
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            sendLocationToServer(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        networkManager = NetworkManager.getInstance()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        childId = intent?.getIntExtra("child_id", -1) ?: -1
        parentId = intent?.getIntExtra("parent_id", -1) ?: -1

        if (childId == -1) {
            Log.e("LocationService", "No child ID provided")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d("LocationService", "Started for child: $childId")

        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Location permission not granted")
            return
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper()
            )

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper()
            )

            // إرسال الموقع الحالي فوراً
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            lastLocation?.let { sendLocationToServer(it) }

        } catch (e: Exception) {
            Log.e("LocationService", "Error starting location updates", e)
        }
    }

    private fun sendLocationToServer(location: Location) {
        val address = getAddressFromLocation(location.latitude, location.longitude)

        networkManager.sendLocation(
            childId = childId,
            latitude = location.latitude,
            longitude = location.longitude,
            address = address,
            onSuccess = {
                Log.d("LocationService", "Location sent successfully")
            },
            onError = { error ->
                Log.e("LocationService", "Failed to send location: $error")
            }
        )
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0].getAddressLine(0)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error getting address", e)
            null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for tracking child location"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, ChildDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مراقب الطفل")
            .setContentText("جاري مشاركة الموقع...")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        Log.d("LocationService", "Service destroyed")
    }
}
