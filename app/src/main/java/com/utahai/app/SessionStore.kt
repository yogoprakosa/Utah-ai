package com.utahai.app

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("yaqutah_session", Context.MODE_PRIVATE)

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_ID, session.user.id)
            .putString(KEY_EMAIL, session.user.email)
            .putString(KEY_NAME, session.user.name)
            .apply()
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    fun user(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        return User(
            id = id,
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            name = prefs.getString(KEY_NAME, "Pengguna") ?: "Pengguna"
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
    }
}
