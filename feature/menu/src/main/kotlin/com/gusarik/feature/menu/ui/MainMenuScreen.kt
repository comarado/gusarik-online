package com.gusarik.feature.menu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onNavigateToPlay: () -> Unit,
    onNavigateToInvite: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    nickname: String = "Игрок"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Text(
            text = "🃏",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "Гусарик Online",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Привет, $nickname!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Menu buttons
        MenuButton(
            text = "Играть",
            icon = Icons.Default.PlayArrow,
            onClick = onNavigateToPlay,
            primary = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
            text = "Пригласить друга",
            icon = Icons.Default.PersonAdd,
            onClick = onNavigateToInvite
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
            text = "История партий",
            icon = Icons.Default.History,
            onClick = onNavigateToHistory
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
            text = "Статистика",
            icon = Icons.Default.BarChart,
            onClick = onNavigateToStats
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
            text = "Настройки",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.weight(1f))

        // Sign out
        TextButton(onClick = onSignOut) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    primary: Boolean = false
) {
    if (primary) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
