# كود تطبيق الأندرويد الموحد - ملف شامل

## 1. build.gradle (Module: app)

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
}

android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.childmonitor"
        minSdk 31
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    
    buildFeatures {
        viewBinding true
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // UI
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // Networking
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Location
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Maps
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    
    // Charts
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // WorkManager
    implementation 'androidx.work:work-runtime-ktx:2.8.1'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
}
```

## 2. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.childmonitor">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.BATTERY_STATS" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.ChildMonitor">

        <activity
            android:name=".activities.SplashActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".activities.ParentLoginActivity"
            android:exported="false" />

        <activity
            android:name=".activities.ChildLoginActivity"
            android:exported="false" />

        <activity
            android:name=".activities.ParentDashboardActivity"
            android:exported="false" />

        <activity
            android:name=".activities.AddChildActivity"
            android:exported="false" />

        <service
            android:name=".services.MonitoringService"
            android:exported="false"
            android:foregroundServiceType="location" />

        <service
            android:name=".services.AccessibilityMonitoringService"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

        <receiver
            android:name=".receivers.CallReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.PHONE_STATE" />
            </intent-filter>
        </receiver>

        <receiver
            android:name=".receivers.SmsReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```

## 3. Models

### Child.kt

```kotlin
package com.example.childmonitor.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class Child(
    @PrimaryKey val id: String,
    val parentId: String,
    val name: String,
    val password: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val batteryLevel: Int = 100,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "locations")
data class LocationData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timestamp: Long
)

@Entity(tableName = "app_usage")
data class AppUsageData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: String,
    val appName: String,
    val packageName: String,
    val usageTime: Int,
    val timestamp: Long
)
```

## 4. Database

### AppDatabase.kt

```kotlin
package com.example.childmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.childmonitor.models.Child
import com.example.childmonitor.models.LocationData
import com.example.childmonitor.models.AppUsageData

@Database(
    entities = [Child::class, LocationData::class, AppUsageData::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun locationDao(): LocationDao
    abstract fun appUsageDao(): AppUsageDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "child_monitor_db"
                ).build().also { instance = it }
            }
        }
    }
}
```

### ChildDao.kt

```kotlin
package com.example.childmonitor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.childmonitor.models.Child

@Dao
interface ChildDao {
    @Insert
    suspend fun insert(child: Child)
    
    @Query("SELECT * FROM children WHERE parentId = :parentId")
    suspend fun getChildrenByParent(parentId: String): List<Child>
    
    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChild(childId: String): Child?
    
    @Query("UPDATE children SET latitude = :lat, longitude = :lon, batteryLevel = :battery, lastUpdate = :timestamp WHERE id = :childId")
    suspend fun updateLocation(childId: String, lat: Double, lon: Double, battery: Int, timestamp: Long)
}
```

## 5. API Client

### ApiClient.kt

```kotlin
package com.example.childmonitor.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiClient {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val baseUrl = "https://your-server.com/api/trpc"
    
    fun loginParent(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        
        sendRequest("parent.login", json, onSuccess, onError)
    }
    
    fun loginChild(
        password: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("password", password)
        }
        
        sendRequest("child.login", json, onSuccess, onError)
    }
    
    fun addChild(
        parentId: String,
        childName: String,
        childPassword: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
            put("name", childName)
            put("password", childPassword)
        }
        
        sendRequest("parent.addChild", json, onSuccess, onError)
    }
    
    fun getChildren(
        parentId: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
        }
        
        sendRequest("parent.getChildren", json, onSuccess, onError)
    }
    
    fun sendLocation(
        childId: String,
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("latitude", latitude)
            put("longitude", longitude)
            put("address", address)
        }
        
        sendRequest("child.sendLocation", json, {}, {})
    }
    
    fun sendAppUsage(
        childId: String,
        appName: String,
        packageName: String,
        usageTime: Int
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("appName", appName)
            put("packageName", packageName)
            put("usageTime", usageTime)
        }
        
        sendRequest("child.sendAppUsage", json, {}, {})
    }
    
    private fun sendRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("method", procedure)
            put("params", data)
            put("id", 1)
        }.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$baseUrl/$procedure")
            .post(requestBody)
            .build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    onSuccess(body)
                } else {
                    onError()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError()
        }
    }
}
```

## 6. Activities

### SplashActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.parentLoginButton.setOnClickListener {
            startActivity(Intent(this, ParentLoginActivity::class.java))
        }
        
        binding.childLoginButton.setOnClickListener {
            startActivity(Intent(this, ChildLoginActivity::class.java))
        }
    }
}
```

### ParentLoginActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentLoginBinding
import com.example.childmonitor.api.ApiClient

class ParentLoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityParentLoginBinding
    private val apiClient = ApiClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            loginParent(email, password)
        }
        
        binding.backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loginParent(email: String, password: String) {
        apiClient.loginParent(
            email,
            password,
            onSuccess = { response ->
                try {
                    val parentId = response.substringAfter("\"parentId\":\"").substringBefore("\"")
                    
                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().apply {
                        putString("parent_id", parentId)
                        putString("parent_email", email)
                        putString("user_type", "parent")
                        apply()
                    }
                    
                    startActivity(Intent(this, ParentDashboardActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "خطأ في معالجة البيانات", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

### ChildLoginActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.api.ApiClient
import com.example.childmonitor.services.MonitoringService

class ChildLoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChildLoginBinding
    private val apiClient = ApiClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.loginButton.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            
            if (password.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال كلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            loginChild(password)
        }
        
        binding.backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loginChild(password: String) {
        apiClient.loginChild(
            password,
            onSuccess = { response ->
                try {
                    val childId = response.substringAfter("\"childId\":\"").substringBefore("\"")
                    
                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().apply {
                        putString("child_id", childId)
                        putString("user_type", "child")
                        apply()
                    }
                    
                    // بدء خدمة المراقبة
                    val intent = Intent(this, MonitoringService::class.java)
                    intent.putExtra("child_id", childId)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    
                    Toast.makeText(this, "تم بدء المراقبة", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "خطأ في معالجة البيانات", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

### ParentDashboardActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityParentDashboardBinding
import com.example.childmonitor.api.ApiClient

class ParentDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityParentDashboardBinding
    private val apiClient = ApiClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val parentId = sharedPref.getString("parent_id", "") ?: ""
        
        loadChildren(parentId)
        
        binding.addChildButton.setOnClickListener {
            // فتح نشاط إضافة طفل
        }
    }
    
    private fun loadChildren(parentId: String) {
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
```

## 7. Services

### MonitoringService.kt

```kotlin
package com.example.childmonitor.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.childmonitor.R
import com.example.childmonitor.monitoring.LocationTracker
import com.example.childmonitor.monitoring.AppUsageMonitor
import com.example.childmonitor.workers.MonitoringWorker
import java.util.concurrent.TimeUnit

class MonitoringService : Service() {
    
    private lateinit var locationTracker: LocationTracker
    private lateinit var appUsageMonitor: AppUsageMonitor
    private var childId: String = ""
    
    companion object {
        const val CHANNEL_ID = "monitoring_channel"
        const val NOTIFICATION_ID = 1
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        childId = intent?.getStringExtra("child_id") ?: ""
        
        if (childId.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        locationTracker = LocationTracker(this)
        appUsageMonitor = AppUsageMonitor(this)
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        locationTracker.startLocationUpdates { lat, lon, accuracy, address ->
            // إرسال البيانات إلى الخادم
        }
        
        schedulePeriodicMonitoring()
        
        return START_STICKY
    }
    
    private fun schedulePeriodicMonitoring() {
        val monitoringWork = PeriodicWorkRequestBuilder<MonitoringWorker>(
            15, TimeUnit.MINUTES
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "monitoring_$childId",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            monitoringWork
        )
    }
    
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("مراقبة الطفل")
        .setContentText("جاري المراقبة...")
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .build()
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "مراقبة الطفل",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopLocationUpdates()
    }
}
```

---

**ملاحظة**: هذا الكود يوفر الأساس. ستحتاج إلى تكييفه حسب احتياجاتك.
