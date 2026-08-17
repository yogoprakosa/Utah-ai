package com.utahai.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    // Production: replace with your HTTPS Utah AI backend.
    // Emulator default is http://10.0.2.2:3000
    private const val BASE_URL = "http://10.0.2.2:3000"

    suspend fun chat(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE_URL/chat").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 15_000
                readTimeout = 60_000
                doOutput = true
            }

            val body = JSONObject().put("message", message).toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            conn.disconnect()

            if (responseCode !in 200..299) {
                Result.failure(Exception("Backend HTTP $responseCode"))
            } else {
                Result.success(JSONObject(response).getString("reply"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
