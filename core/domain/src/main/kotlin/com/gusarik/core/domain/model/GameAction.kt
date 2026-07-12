package com.gusarik.core.domain.model

/**
 * All possible actions a player can take during a game.
 * Sealed class ensures exhaustive when handling.
 */
sealed class GameAction {
    abstract val playerId: String
    abstract val timestamp: Long

    /**
     * Player makes a bid during the auction.
     */
    data class PlaceBid(
        override val playerId: String,
        val bid: Bid,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Winner of the auction chooses a contract.
     */
    data class ChooseContract(
        override val playerId: String,
        val contract: Contract,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Winner of the auction discards 2 cards after seeing the talon.
     */
    data class DiscardTalon(
        override val playerId: String,
        val cards: List<Card>,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Player plays a card during a trick.
     */
    data class PlayCard(
        override val playerId: String,
        val card: Card,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Player sends a chat message.
     */
    data class SendChat(
        override val playerId: String,
        val message: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Player reconnects after disconnection.
     */
    data class Reconnect(
        override val playerId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Player requests to start the next round.
     */
    data class NextRound(
        override val playerId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()

    /**
     * Player requests to leave the game.
     */
    data class LeaveGame(
        override val playerId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameAction()
}

/**
 * Quick chat messages (predefined).
 */
object QuickChat {
    val messages = listOf(
        "Хорошая игра!",
        "Думаю...",
        "Интересный ход!",
        "Привет!",
        "Спасибо за игру!",
        "Не ожидал!",
        "Повезло!",
        "Давай ещё!"
    )
}
