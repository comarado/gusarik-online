package com.gusarik.engine.ai

import com.gusarik.core.domain.model.*

/**
 * Interface for the virtual (NPC) player.
 */
interface VirtualPlayer {
    /**
     * Select a card to play given the current game state.
     * Must be deterministic — same input always produces the same output.
     */
    fun selectCard(hand: List<Card>, currentTrick: PartialTrick, trumpSuit: Suit?): Card

    /**
     * Select a bid during the auction.
     * The NPC always passes in this implementation.
     */
    fun selectBid(currentBids: List<Bid>, hand: List<Card>): Bid
}
