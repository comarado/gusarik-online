package com.gusarik.network.realtime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages automatic reconnection with exponential backoff.
 */
@Singleton
class ReconnectionManager @Inject constructor(
    private val connectionMonitor: ConnectionStateMonitor,
    private val syncManager: GameSyncManager
) {
    private val _reconnectionState = MutableStateFlow<ReconnectionState>(ReconnectionState.Idle)
    val reconnectionState: StateFlow<ReconnectionState> = _reconnectionState.asStateFlow()

    private var reconnectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val INITIAL_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 30000L
        private const val MAX_ATTEMPTS = 10
        private const val BACKOFF_MULTIPLIER = 2.0
    }

    /**
     * Start monitoring and auto-reconnect.
     */
    fun startAutoReconnect(gameCode: String, onReconnect: suspend () -> Unit) {
        reconnectionJob?.cancel()

        reconnectionJob = scope.launch {
            connectionMonitor.isConnected.collect { connected ->
                if (!connected) {
                    syncManager.setConnectionState(SyncState.Disconnected)
                    _reconnectionState.value = ReconnectionState.Disconnected
                } else if (_reconnectionState.value is ReconnectionState.Disconnected ||
                    _reconnectionState.value is ReconnectionState.Reconnecting) {
                    // Connection restored — attempt reconnect
                    attemptReconnect(gameCode, onReconnect)
                }
            }
        }
    }

    /**
     * Attempt to reconnect with exponential backoff.
     */
    private suspend fun attemptReconnect(
        gameCode: String,
        onReconnect: suspend () -> Unit
    ) {
        var attempt = 0
        var delay = INITIAL_DELAY_MS

        while (attempt < MAX_ATTEMPTS) {
            attempt++
            _reconnectionState.value = ReconnectionState.Reconnecting(attempt)
            syncManager.setConnectionState(SyncState.Reconnecting(attempt))

            try {
                onReconnect()
                _reconnectionState.value = ReconnectionState.Connected
                syncManager.setConnectionState(SyncState.Connected)

                // Flush queued actions
                val queuedActions = syncManager.flushQueue()
                if (queuedActions.isNotEmpty()) {
                    // Actions will be replayed by the game engine
                }
                return
            } catch (e: Exception) {
                _reconnectionState.value = ReconnectionState.Failed(e.message ?: "Ошибка")
                delay(delay)
                delay = (delay * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_DELAY_MS)
            }
        }

        _reconnectionState.value = ReconnectionState.Failed("Превышено количество попыток")
    }

    /**
     * Stop auto-reconnect.
     */
    fun stopAutoReconnect() {
        reconnectionJob?.cancel()
        reconnectionJob = null
        _reconnectionState.value = ReconnectionState.Idle
    }

    /**
     * Manually trigger reconnect.
     */
    suspend fun manualReconnect(gameCode: String, onReconnect: suspend () -> Unit) {
        attemptReconnect(gameCode, onReconnect)
    }
}

/**
 * Reconnection state.
 */
sealed class ReconnectionState {
    object Idle : ReconnectionState()
    object Connected : ReconnectionState()
    object Disconnected : ReconnectionState()
    data class Reconnecting(val attempt: Int) : ReconnectionState()
    data class Failed(val reason: String) : ReconnectionState()
}
