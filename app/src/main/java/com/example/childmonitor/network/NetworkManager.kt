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
     */
    private fun sendRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
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

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        Log.d("NetworkManager", "Response: $body")
                        
                        try {
                            val jsonResponse = JSONObject(body)
                            
                            // التحقق من وجود خطأ
                            if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val message = error.optString("message", "حدث خطأ غير معروف")
                                onError(message)
                            } else if (jsonResponse.has("result")) {
                                val result = jsonResponse.getJSONObject("result")
                                onSuccess(result)
                            } else {
                                onError("صيغة الاستجابة غير صحيحة")
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkManager", "Error parsing response", e)
                            onError("خطأ في معالجة الاستجابة: ${e.message}")
                        }
                    } else {
                        Log.e("NetworkManager", "Request failed: ${response.code}")
                        onError("فشل الطلب: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Exception", e)
                onError("خطأ في الاتصال: ${e.message}")
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
