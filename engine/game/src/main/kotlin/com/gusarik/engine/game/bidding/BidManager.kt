package com.gusarik.engine.game.bidding

import com.gusarik.core.domain.model.Bid
import com.gusarik.core.domain.model.BidLadder
import com.gusarik.core.domain.model.BidType
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.engine.validation.ValidationResult

/**
 * Manages the auction/bidding phase of the game.
 */
class BidManager {

    private val bids = mutableListOf<Bid>()
    private var currentBidder: PlayerSeat = PlayerSeat.SOUTH
    private var passCount = 0
    private var auctionComplete = false
    private var winner: PlayerSeat? = null
    private var winningBid: Bid? = null

    /**
     * Start a new auction.
     * @param firstBidder The seat that bids first.
     */
    fun startAuction(firstBidder: PlayerSeat) {
        bids.clear()
        currentBidder = firstBidder
        passCount = 0
        auctionComplete = false
        winner = null
        winningBid = null
    }

    /**
     * Place a bid.
     * @return ValidationResult indicating success or failure.
     */
    fun placeBid(bid: Bid): ValidationResult {
        if (auctionComplete) {
            return ValidationResult.Invalid("Торговля завершена")
        }

        if (bid.player != currentBidder) {
            return ValidationResult.Invalid("Не ваш ход в торговле")
        }

        // Validate bid value
        val validation = com.gusarik.engine.validation.MoveValidator.validateBid(bid, bids, currentBidder)
        if (validation is ValidationResult.Invalid) return validation

        bids.add(bid)

        if (bid.isPass) {
            passCount++
        } else {
            passCount = 0
            winningBid = bid
            winner = currentBidder
        }

        // Check if auction is complete
        // Auction ends when both players pass (распасы) or
        // when one player passes after the other made a bid
        if (passCount >= 2) {
            // Both passed
            auctionComplete = true
            winner = null
            winningBid = null
        } else if (bids.size >= 2) {
            val lastTwo = bids.takeLast(2)
            if (lastTwo[0].isPass && !lastTwo[1].isPass) {
                // One passed, other bid → auction continues
                // Actually: if one player passes and the other has bid,
                // the bidder wins
                auctionComplete = true
            } else if (!lastTwo[0].isPass && lastTwo[1].isPass) {
                // First bid, then second passed → first wins
                auctionComplete = true
            }
        }

        // Advance to next bidder
        if (!auctionComplete) {
            currentBidder = currentBidder.next().let { next ->
                // Skip NPC
                if (next == PlayerSeat.EAST) next.next() else next
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Get the current bidder.
     */
    fun getCurrentBidder(): PlayerSeat = currentBidder

    /**
     * Get all bids made so far.
     */
    fun getBids(): List<Bid> = bids.toList()

    /**
     * Check if the auction is complete.
     */
    fun isComplete(): Boolean = auctionComplete

    /**
     * Get the winner of the auction. Null if both passed (распасы).
     */
    fun getWinner(): PlayerSeat? = winner

    /**
     * Get the winning bid. Null if both passed.
     */
    fun getWinningBid(): Bid? = winningBid

    /**
     * Is this a passed hand (распасы)?
     */
    fun isPassedHand(): Boolean = auctionComplete && winner == null
}
