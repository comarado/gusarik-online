package com.gusarik.core.domain.model

/**
 * A chat message in the game.
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderNickname: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)
