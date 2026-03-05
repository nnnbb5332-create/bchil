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

        sendPostRequest("parent.register", json, onSuccess, onError)
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

        sendPostRequest("parent.login", json, onSuccess, onError)
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

        sendPostRequest("parent.addChild", json, onSuccess, onError)
    }

    /**
     * الحصول على قائمة الأطفال (Query - GET)
     */
    fun getChildren(
        parentId: Int,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("parentId", parentId)
        }

        sendGetRequestForArray("parent.getChildren", json, onSuccess, onError)
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

        sendPostRequest("parent.deleteChild", json, onSuccess, onError)
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

        sendPostRequest("child.login", json, onSuccess, onError)
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

        sendPostRequest("child.sendLocation", json, onSuccess, onError)
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

        sendPostRequest("child.sendAppUsage", json, onSuccess, onError)
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

        sendPostRequest("child.sendCameraImage", json, onSuccess, onError)
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

        sendPostRequest("parent.requestCameraImage", json, onSuccess, onError)
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

        sendGetRequest("parent.getLatestCameraImage", json, onSuccess, onError)
    }

    /**
     * الحصول على آخر موقع للطفل (Query - GET)
     */
    fun getLatestLocation(
        childId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("childId", childId)
        }

        sendGetRequest("parent.getLatestLocation", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل المواقع (Query - GET)
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

        sendGetRequestForArray("parent.getChildLocations", json, onSuccess, onError)
    }

    /**
     * الحصول على سجل استخدام التطبيقات (Query - GET)
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

        sendGetRequestForArray("parent.getChildAppUsage", json, onSuccess, onError)
    }

    /**
     * إرسال طلب POST (Mutations)
     */
    private fun sendPostRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("json", data)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/$procedure")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequest(request, onSuccess, onError)
    }

    /**
     * إرسال طلب GET (Queries)
     */
    private fun sendGetRequest(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val inputJson = JSONObject().apply {
            put("json", data)
        }.toString()
        
        val url = "$baseUrl/$procedure?input=${URLEncoder.encode(inputJson, "UTF-8")}"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")
            .build()

        executeRequest(request, onSuccess, onError)
    }

    /**
     * إرسال طلب GET يُرجع مصفوفة
     */
    private fun sendGetRequestForArray(
        procedure: String,
        data: JSONObject,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val inputJson = JSONObject().apply {
            put("json", data)
        }.toString()
        
        val url = "$baseUrl/$procedure?input=${URLEncoder.encode(inputJson, "UTF-8")}"

        val request = Request.Builder()
            .url(url)
            .get()
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
                    Log.d("NetworkManager", "Response body: $body")

                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(body)
                            
                            if (jsonResponse.has("result")) {
                                val result = jsonResponse.getJSONObject("result")
                                val dataObj = result.getJSONObject("data")
                                val jsonData = dataObj.getJSONObject("json")
                                onSuccess(jsonData)
                            } else if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val message = error.getJSONObject("json").optString("message", "حدث خطأ غير معروف")
                                onError(message)
                            } else {
                                onSuccess(JSONObject()) // استجابة فارغة ناجحة
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkManager", "Error parsing success response", e)
                            onError("خطأ في معالجة الاستجابة: ${e.message}")
                        }
                    } else {
                        handleErrorResponse(body, response.code, onError)
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Network exception", e)
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
                            val jsonResponse = JSONObject(body)
                            
                            if (jsonResponse.has("result")) {
                                val result = jsonResponse.getJSONObject("result")
                                val dataObj = result.getJSONObject("data")
                                val jsonArray = dataObj.getJSONArray("json")
                                onSuccess(jsonArray)
                            } else if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val message = error.getJSONObject("json").optString("message", "حدث خطأ غير معروف")
                                onError(message)
                            } else {
                                onError("صيغة الاستجابة غير صحيحة")
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkManager", "Error parsing array response", e)
                            onError("خطأ في معالجة الاستجابة: ${e.message}")
                        }
                    } else {
                        handleErrorResponse(body, response.code, onError)
                    }
                }
            } catch (e: Exception) {
                Log.e("NetworkManager", "Network exception", e)
                onError("خطأ في الاتصال: ${e.message}")
            }
        }.start()
    }

    private fun handleErrorResponse(body: String, code: Int, onError: (String) -> Unit) {
        try {
            val errorJson = JSONObject(body)
            if (errorJson.has("error")) {
                val error = errorJson.getJSONObject("error")
                val message = error.getJSONObject("json").optString("message", "فشل الطلب")
                onError(message)
            } else {
                onError("فشل الطلب: $code")
            }
        } catch (e: Exception) {
            onError("فشل الطلب: $code")
        }
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
