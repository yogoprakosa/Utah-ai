package com.utahai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YaqutahApp() }
    }
}

@Composable
fun YaqutahApp(vm: UtahAiViewModel = viewModel()) {
    val authState by vm.authState.collectAsState()
    MaterialTheme {
        when (val state = authState) {
            AuthState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            AuthState.SignedOut -> AuthScreen(vm)
            is AuthState.SignedIn -> ChatScreen(vm, state.user)
        }
    }
}

@Composable
private fun AuthScreen(vm: UtahAiViewModel) {
    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading by vm.authLoading.collectAsState()
    val error by vm.authError.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Yaqutah", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text("Asisten AI pribadi kamu", fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))

        if (registerMode) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nama") }, singleLine = true)
            Spacer(Modifier.height(10.dp))
        }
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            password,
            { password = it },
            Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true
        )

        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (registerMode) vm.register(name, email, password)
                else vm.login(email, password)
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank() && (!registerMode || name.isNotBlank()),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (registerMode) "Daftar" else "Masuk")
        }

        TextButton(onClick = {
            vm.clearAuthError()
            registerMode = !registerMode
        }) {
            Text(if (registerMode) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar")
        }
    }
}

@Composable
private fun ChatScreen(vm: UtahAiViewModel, user: User) {
    val messages by vm.messages.collectAsState()
    val loading by vm.loading.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Yaqutah", fontWeight = FontWeight.Bold)
                        Text(user.name, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.logout() }) { Icon(Icons.Default.Logout, "Keluar") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (msg.fromUser) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) { Text(msg.text, Modifier.padding(14.dp)) }
                    }
                }
                if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }

            Row(
                Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tulis pesan...") },
                    shape = RoundedCornerShape(26.dp),
                    singleLine = true
                )
                FloatingActionButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            vm.send(input)
                            input = ""
                        }
                    },
                    shape = CircleShape
                ) { Icon(Icons.Default.Send, "Kirim") }
            }
        }
    }
}
