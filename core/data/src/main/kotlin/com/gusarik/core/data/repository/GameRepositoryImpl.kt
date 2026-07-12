package com.gusarik.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gusarik.core.domain.model.*
import com.gusarik.core.domain.repository.GameRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GameRepository {

    override suspend fun createRoom(hostId: String, settings: GameSettings): Result<String> {
        return try {
            val code = generateRoomCode()
            val room = Room(
                code = code,
                hostId = hostId,
                settings = settings
            )
            firestore.collection("rooms").document(code).set(room).await()
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(roomCode: String, guestId: String): Result<Room> {
        return try {
            val doc = firestore.collection("rooms").document(roomCode).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Комната не найдена"))
            }

            val room = doc.toObject(Room::class.java)
                ?: return Result.failure(Exception("Ошибка чтения комнаты"))

            if (room.guestId != null) {
                return Result.failure(Exception("Комната уже заполнена"))
            }

            firestore.collection("rooms").document(roomCode)
                .update("guestId", guestId, "status", RoomStatus.PLAYING.name).await()

            Result.success(room.copy(guestId = guestId, status = RoomStatus.PLAYING))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRoom(roomCode: String): Flow<Room?> = callbackFlow {
        val listener = firestore.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Room::class.java))
            }
        awaitClose { listener.remove() }
    }

    override fun observeGameState(roomCode: String): Flow<GameState?> = callbackFlow {
        val listener = firestore.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val gameStateMap = snapshot?.get("gameState") as? Map<*, *>
                if (gameStateMap != null) {
                    trySend(deserializeGameState(gameStateMap))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun submitAction(roomCode: String, action: GameAction): Result<Unit> {
        return try {
            val actionData = serializeAction(action)
            firestore.collection("rooms").document(roomCode)
                .collection("moves").add(actionData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveRoom(roomCode: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomCode)
                .update("status", RoomStatus.ABANDONED.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendChatMessage(roomCode: String, message: ChatMessage): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomCode)
                .collection("chat").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeChat(roomCode: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("rooms").document(roomCode)
            .collection("chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateGameState(roomCode: String, state: GameState): Result<Unit> {
        return try {
            val stateMap = serializeGameState(state)
            firestore.collection("rooms").document(roomCode)
                .update("gameState", stateMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeConnectionStatus(): Flow<Boolean> = callbackFlow {
        // Simple connection monitoring
        trySend(true)
        awaitClose { }
    }

    // === Serialization helpers ===

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun serializeAction(action: GameAction): Map<String, Any> {
        return when (action) {
            is GameAction.PlaceBid -> mapOf(
                "type" to "PLACE_BID",
                "playerId" to action.playerId,
                "bidType" to action.bid.type.name,
                "bidLevel" to (action.bid.level ?: 0),
                "bidSuit" to (action.bid.suit?.name ?: ""),
                "timestamp" to action.timestamp
            )
            is GameAction.PlayCard -> mapOf(
                "type" to "PLAY_CARD",
                "playerId" to action.playerId,
                "cardSuit" to action.card.suit.name,
                "cardRank" to action.card.rank.name,
                "timestamp" to action.timestamp
            )
            is GameAction.ChooseContract -> mapOf(
                "type" to "CHOOSE_CONTRACT",
                "playerId" to action.playerId,
                "contractLevel" to action.contract.level,
                "contractSuit" to (action.contract.suit?.name ?: ""),
                "contractMiser" to action.contract.isMiser,
                "timestamp" to action.timestamp
            )
            is GameAction.DiscardTalon -> mapOf(
                "type" to "DISCARD_TALON",
                "playerId" to action.playerId,
                "cards" to action.cards.map { "${it.suit.name}_${it.rank.name}" },
                "timestamp" to action.timestamp
            )
            is GameAction.SendChat -> mapOf(
                "type" to "SEND_CHAT",
                "playerId" to action.playerId,
                "message" to action.message,
                "timestamp" to action.timestamp
            )
            else -> mapOf(
                "type" to action::class.simpleName.orEmpty(),
                "playerId" to action.playerId,
                "timestamp" to action.timestamp
            )
        }
    }

    private fun serializeGameState(state: GameState): Map<String, Any> {
        return mapOf(
            "gameId" to state.gameId,
            "phase" to state.phase.name,
            "roundNumber" to state.roundNumber,
            "scores" to state.scores.mapKeys { it.key.name },
            "trickCounts" to state.trickCounts.mapKeys { it.key.name }
        )
    }

    private fun deserializeGameState(map: Map<*, *>): GameState {
        // Simplified deserialization — full implementation would handle all fields
        return GameState(
            gameId = map["gameId"] as? String ?: "",
            phase = GamePhase.valueOf(map["phase"] as? String ?: "LOBBY"),
            players = emptyMap(),
            hands = emptyMap()
        )
    }
}
