# دليل تطبيق الأندرويد الموحد - نمطا ولي الأمر والطفل

## نظرة عامة

تطبيق أندرويد واحد متكامل يدعم نمطي تسجيل دخول منفصلين:
- **نمط ولي الأمر**: لوحة تحكم كاملة لمراقبة الأطفال
- **نمط الطفل**: خدمة خلفية لجمع البيانات بدون واجهة مرئية

## معمارية التطبيق

```
┌─────────────────────────────────────────┐
│         شاشة البداية                    │
│  (اختيار نمط: ولي أمر أم طفل)          │
└──────────────┬──────────────────────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼             ▼
┌──────────────┐  ┌──────────────┐
│ولي الأمر     │  │الطفل         │
│(تسجيل دخول)  │  │(كلمة مرور)   │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│لوحة التحكم   │  │خدمة خلفية    │
│والخرائط      │  │جمع البيانات  │
└──────────────┘  └──────────────┘
```

## البنية الأساسية للمشروع

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/childmonitor/
│   │   │   ├── activities/
│   │   │   │   ├── SplashActivity.kt          # شاشة البداية
│   │   │   │   ├── ParentLoginActivity.kt     # تسجيل دخول ولي الأمر
│   │   │   │   ├── ChildLoginActivity.kt      # تسجيل دخول الطفل
│   │   │   │   ├── ParentDashboardActivity.kt # لوحة التحكم
│   │   │   │   └── AddChildActivity.kt        # إضافة طفل جديد
│   │   │   ├── services/
│   │   │   │   ├── MonitoringService.kt       # خدمة جمع البيانات
│   │   │   │   └── AccessibilityService.kt    # خدمة الوصول
│   │   │   ├── models/
│   │   │   │   ├── Parent.kt
│   │   │   │   ├── Child.kt
│   │   │   │   ├── Location.kt
│   │   │   │   └── AppUsage.kt
│   │   │   ├── api/
│   │   │   │   └── ApiClient.kt
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   └── Repositories.kt
│   │   │   └── utils/
│   │   │       ├── SharedPreferences.kt
│   │   │       └── Constants.kt
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_splash.xml
│   │       │   ├── activity_parent_login.xml
│   │       │   ├── activity_child_login.xml
│   │       │   ├── activity_parent_dashboard.xml
│   │       │   └── activity_add_child.xml
│   │       └── values/
│   │           └── strings.xml
│   └── AndroidManifest.xml
└── build.gradle
```

## مراحل التطوير

### المرحلة 1: شاشة البداية والتسجيل

#### 1.1 SplashActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.R
import com.example.childmonitor.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.parentLoginButton.setOnClickListener {
            startActivity(Intent(this, ParentLoginActivity::class.java))
            finish()
        }
        
        binding.childLoginButton.setOnClickListener {
            startActivity(Intent(this, ChildLoginActivity::class.java))
            finish()
        }
    }
}
```

#### 1.2 activity_splash.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="@color/white">
    
    <ImageView
        android:id="@+id/appLogo"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/ic_launcher_foreground"
        android:contentDescription="App Logo" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="تطبيق مراقبة الطفل"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginTop="16dp" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="اختر نمط الدخول"
        android:textSize="16sp"
        android:textColor="@color/gray"
        android:layout_marginTop="8dp" />
    
    <Button
        android:id="@+id/parentLoginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="دخول ولي الأمر"
        android:layout_marginTop="32dp"
        android:layout_marginHorizontal="32dp"
        android:backgroundTint="@color/blue" />
    
    <Button
        android:id="@+id/childLoginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="دخول الطفل"
        android:layout_marginTop="16dp"
        android:layout_marginHorizontal="32dp"
        android:backgroundTint="@color/green" />
    
</LinearLayout>
```

### المرحلة 2: تسجيل دخول ولي الأمر

#### 2.1 ParentLoginActivity.kt

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
    private lateinit var apiClient: ApiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiClient = ApiClient()
        
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
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }
    
    private fun loginParent(email: String, password: String) {
        // تنفيذ تسجيل الدخول عبر API
        apiClient.loginParent(email, password) { success, parentId ->
            if (success) {
                // حفظ بيانات ولي الأمر
                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("parent_id", parentId)
                    putString("parent_email", email)
                    putString("user_type", "parent")
                    apply()
                }
                
                startActivity(Intent(this, ParentDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

#### 2.2 activity_parent_login.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@color/white">
    
    <Button
        android:id="@+id/backButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="رجوع"
        android:backgroundTint="@color/gray" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="دخول ولي الأمر"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginTop="32dp" />
    
    <EditText
        android:id="@+id/emailInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="البريد الإلكتروني"
        android:inputType="textEmailAddress"
        android:layout_marginTop="24dp" />
    
    <EditText
        android:id="@+id/passwordInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="كلمة المرور"
        android:inputType="textPassword"
        android:layout_marginTop="16dp" />
    
    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="دخول"
        android:layout_marginTop="24dp"
        android:backgroundTint="@color/blue" />
    
</LinearLayout>
```

### المرحلة 3: تسجيل دخول الطفل

#### 3.1 ChildLoginActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivityChildLoginBinding
import com.example.childmonitor.api.ApiClient
import com.example.childmonitor.services.MonitoringService

class ChildLoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChildLoginBinding
    private lateinit var apiClient: ApiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiClient = ApiClient()
        
        binding.loginButton.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            
            if (password.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال كلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            loginChild(password)
        }
        
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }
    
    private fun loginChild(password: String) {
        // تنفيذ تسجيل دخول الطفل عبر API
        apiClient.loginChild(password) { success, childId ->
            if (success) {
                // حفظ بيانات الطفل
                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("child_id", childId)
                    putString("user_type", "child")
                    apply()
                }
                
                // بدء خدمة المراقبة
                val intent = Intent(this, MonitoringService::class.java)
                intent.putExtra("child_id", childId)
                startForegroundService(intent)
                
                // إغلاق التطبيق من الواجهة (يبقى يعمل في الخلفية)
                finish()
            } else {
                Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

#### 3.2 activity_child_login.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@color/white">
    
    <Button
        android:id="@+id/backButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="رجوع"
        android:backgroundTint="@color/gray" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="دخول الطفل"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginTop="32dp" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="أدخل كلمة المرور التي أعطاك إياها ولي الأمر"
        android:textSize="14sp"
        android:textColor="@color/gray"
        android:layout_marginTop="16dp" />
    
    <EditText
        android:id="@+id/passwordInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="كلمة المرور"
        android:inputType="textPassword"
        android:layout_marginTop="24dp"
        android:textAlignment="center"
        android:textSize="32sp"
        android:letterSpacing="0.5" />
    
    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="دخول"
        android:layout_marginTop="24dp"
        android:backgroundTint="@color/green" />
    
</LinearLayout>
```

### المرحلة 4: لوحة التحكم

#### 4.1 ParentDashboardActivity.kt

```kotlin
package com.example.childmonitor.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.childmonitor.databinding.ActivityParentDashboardBinding
import com.example.childmonitor.api.ApiClient
import com.example.childmonitor.models.Child

class ParentDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityParentDashboardBinding
    private lateinit var apiClient: ApiClient
    private val children = mutableListOf<Child>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiClient = ApiClient()
        
        setupRecyclerView()
        loadChildren()
        
        binding.addChildButton.setOnClickListener {
            // فتح نشاط إضافة طفل جديد
        }
    }
    
    private fun setupRecyclerView() {
        binding.childrenRecyclerView.layoutManager = LinearLayoutManager(this)
        // إعداد adapter للأطفال
    }
    
    private fun loadChildren() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val parentId = sharedPref.getString("parent_id", "") ?: ""
        
        apiClient.getChildren(parentId) { childrenList ->
            children.clear()
            children.addAll(childrenList)
            // تحديث RecyclerView
        }
    }
}
```

#### 4.2 activity_parent_dashboard.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/white">
    
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="@color/blue">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="لوحة التحكم"
            android:textSize="18sp"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:layout_gravity="center" />
    </FrameLayout>
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/childrenRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
    
    <Button
        android:id="@+id/addChildButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="إضافة طفل جديد"
        android:layout_margin="16dp"
        android:backgroundTint="@color/green" />
    
</LinearLayout>
```

## نموذج البيانات

### Child.kt

```kotlin
package com.example.childmonitor.models

data class Child(
    val id: String,
    val name: String,
    val parentId: String,
    val password: String,
    val latitude: Double,
    val longitude: Double,
    val batteryLevel: Int,
    val lastUpdate: Long
)

data class Location(
    val id: String,
    val childId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timestamp: Long
)

data class AppUsage(
    val id: String,
    val childId: String,
    val appName: String,
    val packageName: String,
    val usageTime: Int,
    val timestamp: Long
)
```

## ApiClient.kt

```kotlin
package com.example.childmonitor.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiClient {
    
    private val client = OkHttpClient()
    private val baseUrl = "https://your-server.com/api/trpc"
    
    fun loginParent(email: String, password: String, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        
        sendRequest("parent.login", json) { response ->
            try {
                val parentId = response.getString("parentId")
                callback(true, parentId)
            } catch (e: Exception) {
                callback(false, "")
            }
        }
    }
    
    fun loginChild(password: String, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("password", password)
        }
        
        sendRequest("child.login", json) { response ->
            try {
                val childId = response.getString("childId")
                callback(true, childId)
            } catch (e: Exception) {
                callback(false, "")
            }
        }
    }
    
    fun getChildren(parentId: String, callback: (List<Child>) -> Unit) {
        val json = JSONObject().apply {
            put("parentId", parentId)
        }
        
        sendRequest("parent.getChildren", json) { response ->
            // معالجة البيانات وإرجاع قائمة الأطفال
        }
    }
    
    private fun sendRequest(procedure: String, data: JSONObject, callback: (JSONObject) -> Unit) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("method", procedure)
            put("params", data)
            put("id", 1)
        }.toString().toRequestBody()
        
        val request = Request.Builder()
            .url("$baseUrl/$procedure")
            .post(requestBody)
            .build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        callback(JSONObject(body))
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
```

## الخطوات التالية

1. تحديث قاعدة البيانات لدعم نظام كلمات المرور للأطفال
2. بناء واجهات RecyclerView لعرض الأطفال والبيانات
3. تطبيق Google Maps للخرائط التفاعلية
4. إضافة المخططات البيانية للإحصائيات
5. تطبيق خدمة جمع البيانات الخلفية

---

**ملاحظة**: هذا الدليل يوفر الأساس لبناء التطبيق. ستحتاج إلى تكييف الكود حسب احتياجاتك الخاصة.
