package com.gusarik.core.domain.model

/**
 * The contract chosen by the winner of the auction.
 */
data class Contract(
    val player: PlayerSeat,
    val level: Int,          // 6-10 (number of tricks to take)
    val suit: Suit?,         // null = БК (no trump)
    val isMiser: Boolean = false
) {
    val displayName: String
        get() = when {
            isMiser -> "Мизер"
            suit != null -> "$level${suit.symbol}"
            else -> "${level}БК"
        }

    /**
     * Whether this contract is a "playing" contract (must take >= level tricks).
     */
    val isPlayingContract: Boolean get() = !isMiser

    /**
     * The trump suit for this contract. Null for БК.
     */
    val trumpSuit: Suit? get() = if (isMiser) null else suit

    /**
     * Number of tricks the contractor must take to fulfill the contract.
     */
    val requiredTricks: Int get() = if (isMiser) 0 else level

    companion object {
        fun miser(player: PlayerSeat) = Contract(player, 0, null, isMiser = true)
    }
}
