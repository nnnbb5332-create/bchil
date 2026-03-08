package com.example.childmonitor.network

import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import java.net.URLEncoder

class NetworkManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ✅ تم التحديث: استخدام Android API الجديد
    private val baseUrl = "https://childmonitor-vdtumhvj.manus.space/api/android"

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

        sendPostRequest("parent/register", json, onSuccess, onError)
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

        sendPostRequest("parent/login", json, onSuccess, onError)
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

        sendPostRequest("parent/addChild", json, onSuccess, onError)
    }

    /**
     * الحصول على قائمة الأطفال
     */
    fun getChildren(
        parentId: Int,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
        }

        sendPostRequestForArray("parent/getChildren", json, onSuccess, onError)
    }

    /**
     * حذف طفل
     */
    fun deleteChild(
        childId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendPostRequest("parent/deleteChild", json, onSuccess, onError)
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

        sendPostRequest("child/login", json, onSuccess, onError)
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

        sendPostRequest("child/sendLocation", json, onSuccess, onError)
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

        sendPostRequest("child/sendAppUsage", json, onSuccess, onError)
    }

    /**
     * إرسال صورة ملتقطة من الكاميرا
     */
    fun sendCameraImage(
        childId: Int,
        imageBase64: String,
        onSuccess: (JSONObject) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("image", imageBase64)
        }

        sendPostRequest("child/sendCameraImage", json, onSuccess, onError)
    }

    /**
     * التحقق من وجود طلب التقاط صورة (من الطفل للخادم)
     */
    fun checkCameraRequest(
        childId: Int,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendPostRequest("child/checkCameraRequest", json, { response ->
            val hasRequest = response.optBoolean("hasRequest", false)
            onSuccess(hasRequest)
        }, onError)
    }

    /**
     * طلب التقاط صورة من الكاميرا (من الأب للطفل)
     */
    fun requestCameraImage(
        childId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendPostRequest("parent/requestCameraImage", json, onSuccess, onError)
    }

    /**
     * الحصول على آخر صورة ملتقطة للطفل
     */
    fun getLatestCameraImage(
        childId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendPostRequest("parent/getLatestCameraImage", json, onSuccess, onError)
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

        sendPostRequest("parent/getLatestLocation", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل المواقع
     */
    fun getChildLocations(
        childId: Int,
        limit: Int = 50,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("limit", limit)
        }

        sendPostRequestForArray("parent/getChildLocations", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل استخدام التطبيقات
     */
    fun getChildAppUsage(
        childId: Int,
        limit: Int = 100,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
            put("limit", limit)
        }

        sendPostRequestForArray("parent/getChildAppUsage", json, onSuccess, onError)
    }

    /**
     * إرسال طلب POST (Mutations)
     * ✅ تم التحديث: إرسال البيانات مباشرة بدون wrapper
     */
    private fun sendPostRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ التغيير: إرسال البيانات مباشرة بدون "json" wrapper
        val requestBody = data.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/$procedure")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequest(request, onSuccess, onError)
    }

    /**
     * إرسال طلب POST يُرجع مصفوفة
     */
    private fun sendPostRequestForArray(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ التغيير: إرسال البيانات مباشرة بدون "json" wrapper
        val requestBody = data.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/$procedure")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequestForArray(request, onSuccess, onError)
    }

    /**
     * تنفيذ الطلب ومعالجة الاستجابة (كائن)
     */
    private fun executeRequest(
        request: Request,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                Log.d("NetworkManager", "Sending request to: ${request.url}")

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    Log.d("NetworkManager", "Response code: ${response.code}")

                    if (response.isSuccessful) {
                        try {
                            // التحقق مما إذا كانت الاستجابة تبدأ بـ HTML
                            if (body.trim().startsWith("<!doctype", ignoreCase = true) || body.trim().startsWith("<html", ignoreCase = true)) {
                                Log.e("NetworkManager", "Received HTML instead of JSON: $body")
                                onError("خطأ في السيرفر: استلم التطبيق صفحة HTML بدلاً من بيانات JSON. يرجى التأكد من صحة رابط السيرفر.")
                                return@Thread
                            }

                            val jsonResponse = JSONObject(body)
                            
                            // التحقق من وجود خطأ في الـ JSON
                            if (jsonResponse.has("error") && jsonResponse.get("error") != null && jsonResponse.get("error") != JSONObject.NULL) {
                                val errorObj = jsonResponse.get("error")
                                val errorMessage = if (errorObj is JSONObject) {
                                    errorObj.optString("message", "خطأ غير معروف")
                                } else {
                                    errorObj.toString()
                                }
                                Log.e("NetworkManager", "Error: $errorMessage")
                                onError(errorMessage)
                            } else if (jsonResponse.has("data")) {
                                val data = jsonResponse.get("data")
                                if (data is JSONObject) {
                                    Log.d("NetworkManager", "Success: $data")
                                    onSuccess(data)
                                } else {
                                    // إذا كانت البيانات ليست كائناً (مثلاً Boolean أو String)، نضعها في كائن جديد
                                    val wrapper = JSONObject().put("result", data)
                                    onSuccess(wrapper)
                                }
                            } else {
                                // إذا لم يكن هناك data ولا error، قد تكون البيانات هي الـ JSON نفسه
                                onSuccess(jsonResponse)
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkManager", "Error parsing response", e)
                            onError("خطأ في معالجة الاستجابة: ${e.message}")
                        }
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e("NetworkManager", "Request failed with code: ${response.code}")
                        Log.e("NetworkManager", "Error body: $errorBody")
                        
                        try {
                            val errorJson = JSONObject(errorBody)
                            if (errorJson.has("error")) {
                                val errorMessage = errorJson.getString("error")
                                onError(errorMessage)
                            } else {
                                onError("فشل الطلب: ${response.code}")
                            }
                        } catch (e: Exception) {
                            onError("فشل الطلب: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Exception during request", e)
                onError("خطأ في الاتصال: ${e.message}")
            }
        }.start()
    }

    /**
     * تنفيذ الطلب ومعالجة الاستجابة (مصفوفة)
     */
    private fun executeRequestForArray(
        request: Request,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                Log.d("NetworkManager", "Sending request to: ${request.url}")

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    Log.d("NetworkManager", "Response code: ${response.code}")

                    if (response.isSuccessful) {
                        try {
                            // التحقق مما إذا كانت الاستجابة تبدأ بـ HTML
                            if (body.trim().startsWith("<!doctype", ignoreCase = true) || body.trim().startsWith("<html", ignoreCase = true)) {
                                Log.e("NetworkManager", "Received HTML instead of JSON: $body")
                                onError("خطأ في السيرفر: استلم التطبيق صفحة HTML بدلاً من بيانات JSON.")
                                return@Thread
                            }

                            val jsonResponse = JSONObject(body)
                            
                            if (jsonResponse.has("error") && jsonResponse.get("error") != null && jsonResponse.get("error") != JSONObject.NULL) {
                                val errorObj = jsonResponse.get("error")
                                val errorMessage = if (errorObj is JSONObject) {
                                    errorObj.optString("message", "خطأ غير معروف")
                                } else {
                                    errorObj.toString()
                                }
                                Log.e("NetworkManager", "Error: $errorMessage")
                                onError(errorMessage)
                            } else if (jsonResponse.has("data")) {
                                val data = jsonResponse.get("data")
                                if (data is JSONArray) {
                                    Log.d("NetworkManager", "Success: $data")
                                    onSuccess(data)
                                } else {
                                    onError("البيانات المستلمة ليست مصفوفة")
                                }
                            } else {
                                onError("استجابة غير متوقعة من الخادم")
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkManager", "Error parsing response", e)
                            onError("خطأ في معالجة الاستجابة: ${e.message}")
                        }
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e("NetworkManager", "Request failed with code: ${response.code}")
                        Log.e("NetworkManager", "Error body: $errorBody")
                        
                        try {
                            val errorJson = JSONObject(errorBody)
                            if (errorJson.has("error")) {
                                val errorMessage = errorJson.getString("error")
                                onError(errorMessage)
                            } else {
                                onError("فشل الطلب: ${response.code}")
                            }
                        } catch (e: Exception) {
                            onError("فشل الطلب: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Exception during request", e)
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
