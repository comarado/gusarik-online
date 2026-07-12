package com.gusarik.core.domain.model

/**
 * A card played into a trick by a specific player.
 */
data class TrickCard(
    val player: PlayerSeat,
    val card: Card,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * A completed trick (all 3 cards played).
 */
data class Trick(
    val cards: List<TrickCard>,
    val winner: PlayerSeat,
    val leadSuit: Suit,
    val trickNumber: Int
) {
    /**
     * The card that won this trick.
     */
    val winningCard: Card
        get() = cards.first { it.player == winner }.card

    /**
     * Was this trick won by a trump?
     */
    fun isWonByTrump(trumpSuit: Suit?): Boolean {
        if (trumpSuit == null) return false
        return winningCard.suit == trumpSuit
    }
}

/**
 * Partial trick (cards played so far, before all 3 are down).
 */
data class PartialTrick(
    val cards: List<TrickCard>,
    val leadSuit: Suit? = cards.firstOrNull()?.card?.suit
) {
    val isEmpty: Boolean get() = cards.isEmpty()
    val isComplete: Boolean get() = cards.size == 3
    val currentPlayerCount: Int get() = cards.size

    /**
     * Which seat should play next.
     */
    fun nextPlayer(): PlayerSeat? {
        if (cards.isEmpty()) return null
        return cards.last().player.next()
    }
}
