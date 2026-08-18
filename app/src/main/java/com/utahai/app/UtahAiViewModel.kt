package com.utahai.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Loading : AuthState()
    data object SignedOut : AuthState()
    data class SignedIn(val user: User) : AuthState()
}

data class UiMessage(val text: String, val fromUser: Boolean)

class UtahAiViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(UiMessage("Halo! Saya Yaqutah. Saya siap membantu kamu.", false))
    )
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val token = sessionStore.token()
        val cachedUser = sessionStore.user()
        if (token.isNullOrBlank() || cachedUser == null) {
            _authState.value = AuthState.SignedOut
            return
        }

        viewModelScope.launch {
            ApiClient.me(token).fold(
                onSuccess = { user ->
                    sessionStore.save(AuthSession(token, user))
                    _authState.value = AuthState.SignedIn(user)
                },
                onFailure = {
                    sessionStore.clear()
                    _authState.value = AuthState.SignedOut
                }
            )
        }
    }

    fun login(email: String, password: String) {
        submitAuth {
            ApiClient.login(email.trim(), password)
        }
    }

    fun register(name: String, email: String, password: String) {
        submitAuth {
            ApiClient.register(email.trim(), password, name.trim().ifBlank { "Pengguna" })
        }
    }

    private fun submitAuth(action: suspend () -> Result<AuthSession>) {
        if (_authLoading.value) return
        _authError.value = null
        _authLoading.value = true
        viewModelScope.launch {
            action().fold(
                onSuccess = { session ->
                    sessionStore.save(session)
                    _authState.value = AuthState.SignedIn(session.user)
                },
                onFailure = { error ->
                    _authError.value = error.message ?: "Gagal terhubung ke server."
                }
            )
            _authLoading.value = false
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun logout() {
        val token = sessionStore.token()
        viewModelScope.launch {
            if (!token.isNullOrBlank()) ApiClient.logout(token)
            sessionStore.clear()
            _authState.value = AuthState.SignedOut
            _messages.value = listOf(UiMessage("Halo! Saya Yaqutah. Saya siap membantu kamu.", false))
        }
    }

    fun send(message: String) {
        val clean = message.trim()
        val token = sessionStore.token()
        if (clean.isEmpty() || _loading.value || token.isNullOrBlank()) return

        _messages.value += UiMessage(clean, true)
        _loading.value = true

        viewModelScope.launch {
            val result = ApiClient.chat(clean, token)
            _messages.value += UiMessage(
                result.getOrElse { error ->
                    if (error is ApiException && error.code == 401) {
                        sessionStore.clear()
                        _authState.value = AuthState.SignedOut
                        "Sesi login sudah tidak berlaku. Silakan login kembali."
                    } else {
                        error.message ?: "Yaqutah belum dapat terhubung ke server."
                    }
                },
                false
            )
            _loading.value = false
        }
    }
}
