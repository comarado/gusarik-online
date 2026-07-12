package com.gusarik.core.domain.model

/**
 * Type of bid in the auction.
 */
enum class BidType {
    PASS,
    SUIT_BID,      // Regular bid with a suit
    NO_TRUMP,      // БК (Без Козыря)
    MISER          // Мизер
}

/**
 * A single bid made by a player during the auction.
 */
data class Bid(
    val player: PlayerSeat,
    val type: BidType,
    val level: Int? = null,      // 6-10 for regular bids
    val suit: Suit? = null       // null for NO_TRUMP or MISER
) {
    /**
     * Numeric representation for comparison.
     * Higher value = stronger bid.
     */
    val numericValue: Int
        get() = when (type) {
            BidType.PASS -> -1
            BidType.MISER -> 1000
            BidType.NO_TRUMP -> (level ?: 0) * 5 + 4
            BidType.SUIT_BID -> (level ?: 0) * 5 + (suit?.ordinal ?: 0)
        }

    val isPass: Boolean get() = type == BidType.PASS
    val isMiser: Boolean get() = type == BidType.MISER

    override fun toString(): String = when (type) {
        BidType.PASS -> "Пас"
        BidType.MISER -> "Мизер"
        BidType.NO_TRUMP -> "${level}БК"
        BidType.SUIT_BID -> "${level}${suit?.symbol ?: "?"}"
    }

    companion object {
        fun pass(player: PlayerSeat) = Bid(player, BidType.PASS)
        fun miser(player: PlayerSeat) = Bid(player, BidType.MISER)
        fun noTrump(player: PlayerSeat, level: Int) = Bid(player, BidType.NO_TRUMP, level)
        fun suitBid(player: PlayerSeat, level: Int, suit: Suit) = Bid(player, BidType.SUIT_BID, level, suit)
    }
}

/**
 * The complete bid ladder for Preferans.
 * Ordered from weakest to strongest.
 */
object BidLadder {
    /**
     * Generate the full bid ladder from 6♠ to 10БК.
     */
    fun fullLadder(): List<Bid> {
        val bids = mutableListOf<Bid>()
        for (level in 6..10) {
            for (suit in Suit.entries) {
                bids.add(Bid(PlayerSeat.SOUTH, BidType.SUIT_BID, level, suit))
            }
            bids.add(Bid(PlayerSeat.SOUTH, BidType.NO_TRUMP, level))
        }
        return bids
    }

    /**
     * Get all bids stronger than the given bid.
     */
    fun bidsAbove(currentBid: Bid): List<Bid> {
        val ladder = fullLadder()
        val currentValue = currentBid.numericValue
        return ladder.filter { it.numericValue > currentValue }
    }

    /**
     * Get the minimum bid (6♠).
     */
    fun minimumBid(): Bid = Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES)

    /**
     * Check if bid A is stronger than bid B.
     */
    fun isStrongerThan(a: Bid, b: Bid): Boolean = a.numericValue > b.numericValue
}
