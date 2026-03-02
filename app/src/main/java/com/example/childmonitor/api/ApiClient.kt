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

        Thread {
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
        }.start()
    }
}
