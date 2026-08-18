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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
        setContent {
            UtahAIApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtahAIApp(vm: UtahAiViewModel = viewModel()) {
    val messages by vm.messages.collectAsState()
    val loading by vm.loading.collectAsState()

    var input by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Utah AI",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Assistant",
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        Box {
                            IconButton(
                                onClick = {
                                    menuOpen = !menuOpen
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }

                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = {
                                    menuOpen = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text("Chat Baru")
                                    },
                                    onClick = {
                                        menuOpen = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.fromUser) {
                                Arrangement.End
                            } else {
                                Arrangement.Start
                            }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (msg.fromUser) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = msg.text,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    if (loading) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Bicara"
                        )
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Tulis pesan...")
                        },
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
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim"
                        )
                    }
                }
            }
        }
    }
}
