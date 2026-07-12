package com.gusarik.core.domain.repository

import com.gusarik.core.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Game repository interface for real-time game operations.
 */
interface GameRepository {
    /**
     * Create a new game room.
     * @return Room code (6 characters).
     */
    suspend fun createRoom(hostId: String, settings: GameSettings = GameSettings()): Result<String>

    /**
     * Join an existing room by code.
     */
    suspend fun joinRoom(roomCode: String, guestId: String): Result<Room>

    /**
     * Observe room state changes in real-time.
     */
    fun observeRoom(roomCode: String): Flow<Room?>

    /**
     * Observe game state changes in real-time.
     */
    fun observeGameState(roomCode: String): Flow<GameState?>

    /**
     * Submit a game action (bid, play card, etc.)
     */
    suspend fun submitAction(roomCode: String, action: GameAction): Result<Unit>

    /**
     * Leave a room.
     */
    suspend fun leaveRoom(roomCode: String, userId: String): Result<Unit>

    /**
     * Send a chat message.
     */
    suspend fun sendChatMessage(roomCode: String, message: ChatMessage): Result<Unit>

    /**
     * Observe chat messages.
     */
    fun observeChat(roomCode: String): Flow<List<ChatMessage>>

    /**
     * Update game state (for host/sync).
     */
    suspend fun updateGameState(roomCode: String, state: GameState): Result<Unit>

    /**
     * Check connection status.
     */
    fun observeConnectionStatus(): Flow<Boolean>
}
