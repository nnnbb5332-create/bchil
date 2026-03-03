package com.example.childmonitor.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NetworkManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://3000-iv9kwo40euydbcxtan4zz-1826f61a.sg1.manus.computer/api/trpc"

    /**
     * تسجيل حساب جديد للآباء
     */
    fun registerParent(
        email: String,
        password: String,
        name: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
        }

        sendRequest("parent.register", json, onSuccess, onError)
    }

    /**
     * تسجيل دخول الآباء
     */
    fun loginParent(
        email: String,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        sendRequest("parent.login", json, onSuccess, onError)
    }

    /**
     * إضافة طفل جديد
     */
    fun addChild(
        parentId: Int,
        name: String,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
            put("name", name)
            put("password", password)
        }

        sendRequest("parent.addChild", json, onSuccess, onError)
    }

    /**
     * الحصول على قائمة الأطفال
     */
    fun getChildren(
        parentId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
        }

        sendRequest("parent.getChildren", json, onSuccess, onError)
    }

    /**
     * تسجيل دخول الطفل
     */
    fun loginChild(
        childId: Int,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("password", password)
        }

        sendRequest("child.login", json, onSuccess, onError)
    }

    /**
     * إرسال موقع الطفل
     */
    fun sendLocation(
        childId: Int,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        onSuccess: (JSONObject) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("latitude", latitude)
            put("longitude", longitude)
            if (address != null) {
                put("address", address)
            }
        }

        sendRequest("child.sendLocation", json, onSuccess, onError)
    }

    /**
     * إرسال بيانات استخدام التطبيقات
     */
    fun sendAppUsage(
        childId: Int,
        appName: String,
        packageName: String,
        usageTime: Int,
        onSuccess: (JSONObject) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("appName", appName)
            put("packageName", packageName)
            put("usageTime", usageTime)
        }

        sendRequest("child.sendAppUsage", json, onSuccess, onError)
    }

    /**
     * الحصول على آخر موقع للطفل
     */
    fun getLatestLocation(
        childId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendRequest("parent.getLatestLocation", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل المواقع
     */
    fun getChildLocations(
        childId: Int,
        limit: Int = 50,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("limit", limit)
        }

        sendRequest("parent.getChildLocations", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل استخدام التطبيقات
     */
    fun getChildAppUsage(
        childId: Int,
        limit: Int = 100,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("limit", limit)
        }

        sendRequest("parent.getChildAppUsage", json, onSuccess, onError)
    }

    /**
     * إرسال طلب JSON-RPC 2.0
     * 
     * الصيغة الصحيحة:
     * {
     *   "jsonrpc": "2.0",
     *   "method": "parent.register",
     *   "params": { "email": "...", "password": "...", "name": "..." },
     *   "id": 1
     * }
     */
    private fun sendRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ الصيغة الجديدة لـ tRPC v11
        val requestBody = JSONObject().apply {
            put("json", data)  // البيانات داخل مفتاح "json"
        }.toString().toRequestBody("application/json".toMediaType())
    
        // ✅ الرابط يجب أن يحتوي على اسم الإجراء
        val request = Request.Builder()
            .url("$baseUrl/$procedure")  // مثال: /api/trpc/parent.login
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()
    
        Thread {
            try {
                Log.d("NetworkManager", "Sending request to: $baseUrl/$procedure")
                Log.d("NetworkManager", "Data: $data")
    
                client.newCall(request).execute().use { response ->
                    Log.d("NetworkManager", "Response code: ${response.code}")
                    val body = response.body?.string() ?: ""
                    Log.d("NetworkManager", "Response body: $body")
    
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(body)
                            // ✅ الاستجابة تأتي ضمن result.data.json
                            if (jsonResponse.has("result")) {
                                val result = jsonResponse.getJSONObject("result")
                                val data = result.getJSONObject("data").getJSONObject("json")
                                onSuccess(data)
                            } else if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val message = error.getJSONObject("json").optString("message", "حدث خطأ")
                                onError(message)
                            }
                        } catch (e: Exception) {
                            onError("خطأ في معالجة الاستجابة")
                        }
                    } else {
                        // معالجة أخطاء HTTP
                        try {
                            val errorJson = JSONObject(body)
                            val message = errorJson.getJSONObject("error").getJSONObject("json").optString("message", "فشل الطلب")
                            onError(message)
                        } catch (e: Exception) {
                            onError("فشل الطلب: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Exception", e)
                onError("خطأ في الاتصال")
            }
        }.start()
    }

    companion object {
        private var instance: NetworkManager? = null

        fun getInstance(): NetworkManager {
            if (instance == null) {
                instance = NetworkManager()
            }
            return instance!!
        }
    }
}
