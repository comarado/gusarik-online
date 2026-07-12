package com.gusarik.engine.ai

import com.gusarik.core.domain.model.*

/**
 * Simple deterministic virtual player algorithm.
 *
 * Card selection rules (in priority order):
 * 1. If must follow suit → play lowest card of that suit
 * 2. If can't follow suit and has trump → play lowest trump
 * 3. If no trump → play lowest card overall
 *
 * This is intentionally simple and deterministic.
 * Same game state always produces the same card choice.
 */
class SimpleVirtualPlayer : VirtualPlayer {

    override fun selectCard(
        hand: List<Card>,
        currentTrick: PartialTrick,
        trumpSuit: Suit?
    ): Card {
        require(hand.isNotEmpty()) { "NPC hand is empty" }

        val leadSuit = currentTrick.leadSuit

        return when {
            // First card of trick — play lowest card overall
            leadSuit == null -> playLowestCard(hand)

            // Has cards of the lead suit — play lowest of that suit
            hand.any { it.suit == leadSuit } -> playLowestOfSuit(hand, leadSuit)

            // Can't follow suit, has trump — play lowest trump
            trumpSuit != null && hand.any { it.suit == trumpSuit } ->
                playLowestOfSuit(hand, trumpSuit)

            // No lead suit, no trump — play lowest card overall
            else -> playLowestCard(hand)
        }
    }

    override fun selectBid(currentBids: List<Bid>, hand: List<Card>): Bid {
        // NPC always passes — it doesn't participate in the auction
        return Bid.pass(PlayerSeat.EAST)
    }

    /**
     * Play the lowest card of a specific suit.
     */
    private fun playLowestOfSuit(hand: List<Card>, suit: Suit): Card {
        return hand
            .filter { it.suit == suit }
            .minByOrNull { it.rank.value }
            ?: hand.minByOrNull { it.rank.value }!!
    }

    /**
     * Play the absolute lowest card in hand.
     */
    private fun playLowestCard(hand: List<Card>): Card {
        return hand.minByOrNull { it.rank.value }!!
    }

    companion object {
        /**
         * Seed for deterministic behavior if needed.
         * Currently unused as the algorithm is fully deterministic by design.
         */
        const val VERSION = 1
    }
}
