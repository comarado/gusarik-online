package com.gusarik.feature.lobby.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LobbyScreen(
    onNavigateToGame: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    // Navigate when game starts
    LaunchedEffect(uiState.room) {
        uiState.room?.let { room ->
            if (room.guestId != null) {
                onNavigateToGame(room.code)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Лобби",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.roomCode != null && uiState.isWaiting) {
            // Waiting for opponent
            WaitingForOpponent(
                roomCode = uiState.roomCode!!,
                onCopyCode = {
                    clipboardManager.setText(AnnotatedString(uiState.roomCode!!))
                }
            )
        } else {
            // Create or Join
            CreateOrJoinSection(
                joinCode = uiState.joinCode,
                onJoinCodeChange = viewModel::updateJoinCode,
                onCreateRoom = viewModel::createRoom,
                onJoinRoom = viewModel::joinRoom,
                isLoading = uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }
    }
}

@Composable
private fun WaitingForOpponent(
    roomCode: String,
    onCopyCode: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ожидание соперника...",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Код комнаты",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = roomCode,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(onClick = onCopyCode) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Копировать")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Отправьте код другу для подключения",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreateOrJoinSection(
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Create room
        Button(
            onClick = onCreateRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            Text("Создать комнату", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()
        Text(
            text = "или",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        // Join room
        OutlinedTextField(
            value = joinCode,
            onValueChange = { if (it.length <= 6) onJoinCodeChange(it) },
            label = { Text("Код комнаты") },
            placeholder = { Text("ABCDEF") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onJoinRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && joinCode.length == 6
        ) {
            Text("Присоединиться", style = MaterialTheme.typography.titleMedium)
        }
    }
}
