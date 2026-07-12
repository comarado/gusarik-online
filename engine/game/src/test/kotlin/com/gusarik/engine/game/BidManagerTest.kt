package com.gusarik.engine.game

import com.gusarik.core.domain.model.Bid
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.core.domain.model.Suit
import com.gusarik.engine.game.bidding.BidManager
import com.gusarik.engine.validation.ValidationResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BidManagerTest {

    private lateinit var bidManager: BidManager

    @Before
    fun setup() {
        bidManager = BidManager()
        bidManager.startAuction(PlayerSeat.SOUTH)
    }

    @Test
    fun `initial bidder is correct`() {
        assertEquals(PlayerSeat.SOUTH, bidManager.getCurrentBidder())
    }

    @Test
    fun `pass is always valid`() {
        val result = bidManager.placeBid(Bid.pass(PlayerSeat.SOUTH))
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `first bid must be at least 6 spades`() {
        val result = bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES))
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `cannot bid out of turn`() {
        val result = bidManager.placeBid(Bid.suitBid(PlayerSeat.NORTH, 6, Suit.SPADES))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `bidding advances to next player`() {
        bidManager.placeBid(Bid.pass(PlayerSeat.SOUTH))
        assertEquals(PlayerSeat.NORTH, bidManager.getCurrentBidder())
    }

    @Test
    fun `both players pass results in passed hand`() {
        bidManager.placeBid(Bid.pass(PlayerSeat.SOUTH))
        bidManager.placeBid(Bid.pass(PlayerSeat.NORTH))

        assertTrue(bidManager.isComplete())
        assertTrue(bidManager.isPassedHand())
        assertNull(bidManager.getWinner())
    }

    @Test
    fun `one pass one bid results in winner`() {
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES))
        bidManager.placeBid(Bid.pass(PlayerSeat.NORTH))

        assertTrue(bidManager.isComplete())
        assertFalse(bidManager.isPassedHand())
        assertEquals(PlayerSeat.SOUTH, bidManager.getWinner())
    }

    @Test
    fun `higher bid beats lower bid`() {
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES))
        val result = bidManager.placeBid(Bid.suitBid(PlayerSeat.NORTH, 6, Suit.CLUBS))

        assertTrue(result is ValidationResult.Valid)
        assertEquals(PlayerSeat.NORTH, bidManager.getCurrentBidder())
    }

    @Test
    fun `cannot bid lower or equal to current highest`() {
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.HEARTS))
        val result = bidManager.placeBid(Bid.suitBid(PlayerSeat.NORTH, 6, Suit.SPADES))

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `bidding ladder order is correct`() {
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES))
        bidManager.placeBid(Bid.suitBid(PlayerSeat.NORTH, 6, Suit.CLUBS))
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.DIAMONDS))
        bidManager.placeBid(Bid.pass(PlayerSeat.NORTH))

        assertTrue(bidManager.isComplete())
        assertEquals(PlayerSeat.SOUTH, bidManager.getWinner())
        assertEquals(6, bidManager.getWinningBid()?.level)
        assertEquals(Suit.DIAMONDS, bidManager.getWinningBid()?.suit)
    }

    @Test
    fun `no trump bid is higher than heart bid`() {
        bidManager.placeBid(Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.HEARTS))
        val result = bidManager.placeBid(Bid.noTrump(PlayerSeat.NORTH, 6))

        assertTrue(result is ValidationResult.Valid)
    }
}
