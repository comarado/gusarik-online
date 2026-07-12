package com.gusarik.engine.game.playing

import com.gusarik.core.domain.model.PartialTrick
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.core.domain.model.Suit

/**
 * Determines the winner of a trick.
 * Follows standard Preferans trick-winning rules.
 */
object TrickResolver {

    /**
     * Resolve which player wins the trick.
     *
     * Rules:
     * 1. If any trump cards were played, the highest trump wins.
     * 2. Otherwise, the highest card of the lead suit wins.
     *
     * @param trick The completed trick (must have 3 cards).
     * @param trumpSuit The trump suit for this contract (null for БК).
     * @return The seat of the winning player.
     */
    fun resolveTrick(trick: PartialTrick, trumpSuit: Suit?): PlayerSeat {
        require(trick.isComplete) { "Trick must be complete to resolve" }

        val cards = trick.cards
        val leadSuit = trick.leadSuit ?: cards.first().card.suit

        // Find all trump cards played
        val trumpCards = if (trumpSuit != null) {
            cards.filter { it.card.suit == trumpSuit }
        } else {
            emptyList()
        }

        return if (trumpCards.isNotEmpty()) {
            // Trump was played — highest trump wins
            trumpCards.maxByOrNull { it.card.rank.value }!!.player
        } else {
            // No trump — highest card of lead suit wins
            cards.filter { it.card.suit == leadSuit }
                .maxByOrNull { it.card.rank.value }!!.player
        }
    }

    /**
     * Determine who should lead the first trick of a round.
     * In Preferans, the player to the left of the declarer leads.
     * But in Гусарик, the declarer leads.
     */
    fun firstTrickLeader(declarer: PlayerSeat): PlayerSeat {
        return declarer
    }
}
