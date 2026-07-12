package com.gusarik.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gusarik.core.domain.model.ScoringSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var selectedScoring by remember { mutableStateOf(ScoringSystem.BULLET) }
    var turnTimeLimit by remember { mutableStateOf(60) }
    var darkTheme by remember { mutableStateOf(false) }
    var chatEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scoring system
            Text(
                text = "Система подсчёта",
                style = MaterialTheme.typography.titleMedium
            )
            ScoringSystem.entries.forEach { system ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedScoring == system,
                        onClick = { selectedScoring = system }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(system.displayName)
                }
            }

            HorizontalDivider()

            // Turn time limit
            Text(
                text = "Лимит времени на ход: $turnTimeLimit сек",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = turnTimeLimit.toFloat(),
                onValueChange = { turnTimeLimit = it.toInt() },
                valueRange = 30f..120f,
                steps = 3
            )

            HorizontalDivider()

            // Dark theme
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Тёмная тема",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { darkTheme = it }
                )
            }

            // Chat
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Чат в игре",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = chatEnabled,
                    onCheckedChange = { chatEnabled = it }
                )
            }
        }
    }
}
