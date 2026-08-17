package com.utahai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiMessage(val text: String, val fromUser: Boolean)

class UtahAiViewModel : ViewModel() {
    private val _messages = MutableStateFlow(
        listOf(UiMessage("Halo! Saya Utah AI. Saya siap membantu kamu.", false))
    )
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun send(message: String) {
        val clean = message.trim()
        if (clean.isEmpty() || _loading.value) return

        _messages.value += UiMessage(clean, true)
        _loading.value = true

        viewModelScope.launch {
            val result = ApiClient.chat(clean)
            _messages.value += UiMessage(
                result.getOrElse { "Utah AI belum dapat terhubung ke server. Periksa backend dan koneksi internet." },
                false
            )
            _loading.value = false
        }
    }
}
