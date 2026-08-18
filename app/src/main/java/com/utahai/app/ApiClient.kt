package com.utahai.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "https://utah-ai-backend.yogo-prakosa.workers.dev"

    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        token: String? = null
    ): JSONObject {
        val conn = (URL("$BASE_URL$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            token?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
            connectTimeout = 15_000
            readTimeout = 60_000
            doInput = true
            doOutput = body != null
        }

        try {
            if (body != null) {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = if (response.isBlank()) JSONObject() else JSONObject(response)

            if (responseCode !in 200..299) {
                throw ApiException(responseCode, json.optString("error", "Backend HTTP $responseCode"))
            }

            return json
        } finally {
            conn.disconnect()
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<AuthSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = request(
                    "POST",
                    "/auth/register",
                    JSONObject()
                        .put("email", email)
                        .put("password", password)
                        .put("name", name)
                )
                AuthSession.fromJson(json)
            }
        }

    suspend fun login(email: String, password: String): Result<AuthSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = request(
                    "POST",
                    "/auth/login",
                    JSONObject()
                        .put("email", email)
                        .put("password", password)
                )
                AuthSession.fromJson(json)
            }
        }

    suspend fun me(token: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val json = request("GET", "/auth/me", token = token)
            User.fromJson(json.getJSONObject("user"))
        }
    }

    suspend fun logout(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request("POST", "/auth/logout", token = token)
            Unit
        }
    }

    suspend fun chat(message: String, token: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val json = request(
                "POST",
                "/chat",
                JSONObject().put("message", message),
                token
            )
            json.getString("reply")
        }
    }
}

data class User(
    val id: String,
    val email: String,
    val name: String
) {
    companion object {
        fun fromJson(json: JSONObject) = User(
            id = json.optString("id"),
            email = json.optString("email"),
            name = json.optString("name", "Pengguna")
        )
    }
}

data class AuthSession(
    val token: String,
    val user: User
) {
    companion object {
        fun fromJson(json: JSONObject): AuthSession {
            val token = json.optString("token")
            if (token.isBlank()) throw ApiException(200, "Token sesi tidak diterima server.")
            return AuthSession(
                token = token,
                user = User.fromJson(json.getJSONObject("user"))
            )
        }
    }
}

class ApiException(val code: Int, message: String) : Exception(message)
