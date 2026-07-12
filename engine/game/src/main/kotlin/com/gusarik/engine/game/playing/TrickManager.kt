package com.gusarik.engine.game.playing

import com.gusarik.core.domain.model.*
import com.gusarik.engine.validation.MoveValidator
import com.gusarik.engine.validation.ValidationResult

/**
 * Manages trick-taking during the play phase.
 */
class TrickManager {

    private var currentTrick = PartialTrick(emptyList())
    private val completedTricks = mutableListOf<Trick>()
    private var trickNumber = 0

    /**
     * Start a new trick.
     * @param leader The player who leads the trick.
     */
    fun startTrick(leader: PlayerSeat) {
        currentTrick = PartialTrick(emptyList())
    }

    /**
     * Play a card into the current trick.
     * @return ValidationResult indicating success or failure.
     */
    fun playCard(
        card: Card,
        player: PlayerSeat,
        hand: List<Card>,
        trumpSuit: Suit?,
        contract: Contract?
    ): ValidationResult {
        // Validate the card play
        val validation = MoveValidator.validateCardPlay(card, hand, currentTrick, trumpSuit, contract)
        if (validation is ValidationResult.Invalid) return validation

        // Add card to current trick
        currentTrick = currentTrick.copy(
            cards = currentTrick.cards + TrickCard(player, card)
        )

        return ValidationResult.Valid
    }

    /**
     * Check if the current trick is complete (all 3 cards played).
     */
    fun isTrickComplete(): Boolean = currentTrick.isComplete

    /**
     * Resolve the current trick and determine the winner.
     * @return The completed trick, or null if not yet complete.
     */
    fun resolveTrick(trumpSuit: Suit?): Trick? {
        if (!isTrickComplete()) return null

        val winner = TrickResolver.resolveTrick(currentTrick, trumpSuit)
        trickNumber++

        val trick = Trick(
            cards = currentTrick.cards,
            winner = winner,
            leadSuit = currentTrick.leadSuit ?: currentTrick.cards.first().card.suit,
            trickNumber = trickNumber
        )

        completedTricks.add(trick)
        currentTrick = PartialTrick(emptyList())

        return trick
    }

    /**
     * Get the current trick being played.
     */
    fun getCurrentTrick(): PartialTrick = currentTrick

    /**
     * Get all completed tricks.
     */
    fun getCompletedTricks(): List<Trick> = completedTricks.toList()

    /**
     * Get trick counts per player.
     */
    fun getTrickCounts(): Map<PlayerSeat, Int> {
        val counts = mutableMapOf(
            PlayerSeat.SOUTH to 0,
            PlayerSeat.NORTH to 0,
            PlayerSeat.EAST to 0
        )
        for (trick in completedTricks) {
            counts[trick.winner] = (counts[trick.winner] ?: 0) + 1
        }
        return counts
    }

    /**
     * Get the player who should lead the next trick.
     */
    fun getNextLeader(): PlayerSeat? {
        return if (completedTricks.isEmpty()) {
            null // Will be set by game engine
        } else {
            completedTricks.last().winner
        }
    }

    /**
     * Is the round complete (all 10 tricks played)?
     */
    fun isRoundComplete(): Boolean = completedTricks.size >= 10

    /**
     * Reset for a new round.
     */
    fun reset() {
        currentTrick = PartialTrick(emptyList())
        completedTricks.clear()
        trickNumber = 0
    }
}
