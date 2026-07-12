package com.gusarik.engine.game.dealing

import com.gusarik.core.domain.model.Card

/**
 * A 32-card Preferans deck.
 */
class Deck {
    private var cards: MutableList<Card> = Card.fullDeck().toMutableList()

    /**
     * Shuffle the deck using Fisher-Yates algorithm.
     * Uses a seed for deterministic shuffling in tests.
     */
    fun shuffle(seed: Long? = null) {
        cards = Card.fullDeck().toMutableList()
        val random = if (seed != null) java.util.Random(seed) else java.util.Random()
        for (i in cards.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = cards[i]
            cards[i] = cards[j]
            cards[j] = temp
        }
    }

    /**
     * Deal cards for a 3-player game.
     * Returns: Triple(hand1, hand2, hand3) where each hand has 10 cards.
     * The remaining 2 cards are the talon.
     */
    fun deal(): DealResult {
        require(cards.size == 32) { "Deck must have 32 cards" }

        val hand1 = cards.subList(0, 10).sortedWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
        val hand2 = cards.subList(10, 20).sortedWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
        val hand3 = cards.subList(20, 30).sortedWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
        val talon = cards.subList(30, 32).toList()

        return DealResult(hand1, hand2, hand3, talon)
    }

    /**
     * Get remaining cards (for verification).
     */
    fun remaining(): List<Card> = cards.toList()

    val size: Int get() = cards.size
}

/**
 * Result of dealing cards.
 */
data class DealResult(
    val hand1: List<Card>,  // Player 1 (SOUTH)
    val hand2: List<Card>,  // Player 2 (NORTH)
    val hand3: List<Card>,  // Player 3 (EAST - NPC)
    val talon: List<Card>   // 2 cards for the talon
)
