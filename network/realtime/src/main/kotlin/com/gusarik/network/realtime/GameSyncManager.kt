package com.gusarik.network.realtime

import com.gusarik.core.domain.model.GameAction
import com.gusarik.core.domain.model.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages real-time game synchronization.
 * Handles action queuing, conflict resolution, and state sync.
 */
@Singleton
class GameSyncManager @Inject constructor() {

    private val _connectionState = MutableStateFlow<SyncState>(SyncState.Connected)
    val connectionState: StateFlow<SyncState> = _connectionState.asStateFlow()

    private val actionQueue = mutableListOf<GameAction>()
    private var lastSequenceNumber = 0L

    /**
     * Queue an action for synchronization.
     * Actions are held while offline and replayed on reconnect.
     */
    fun queueAction(action: GameAction) {
        if (_connectionState.value is SyncState.Disconnected) {
            actionQueue.add(action)
        }
    }

    /**
     * Process the action queue after reconnection.
     * Returns actions that need to be replayed.
     */
    fun flushQueue(): List<GameAction> {
        val actions = actionQueue.toList()
        actionQueue.clear()
        return actions
    }

    /**
     * Update connection state.
     */
    fun setConnectionState(state: SyncState) {
        _connectionState.value = state
    }

    /**
     * Check if currently connected.
     */
    fun isConnected(): Boolean = _connectionState.value is SyncState.Connected

    /**
     * Get the next sequence number for ordering actions.
     */
    fun nextSequenceNumber(): Long = ++lastSequenceNumber
}

/**
 * Connection synchronization state.
 */
sealed class SyncState {
    object Connected : SyncState()
    object Disconnected : SyncState()
    data class Reconnecting(val attempt: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}
