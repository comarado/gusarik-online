package com.gusarik.engine.game

import com.gusarik.core.domain.model.*
import com.gusarik.engine.ai.SimpleVirtualPlayer
import org.junit.Assert.*
import org.junit.Test

class VirtualPlayerTest {

    private val ai = SimpleVirtualPlayer()

    @Test
    fun `plays lowest card of lead suit when following`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.SPADES, Rank.ACE),
            Card(Suit.HEARTS, Rank.KING)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.TEN)))
        )

        val card = ai.selectCard(hand, trick, trumpSuit = Suit.HEARTS)

        assertEquals(Card(Suit.SPADES, Rank.SEVEN), card)
    }

    @Test
    fun `plays lowest trump when cannot follow suit`() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.CLUBS, Rank.ACE)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.TEN)))
        )

        val card = ai.selectCard(hand, trick, trumpSuit = Suit.HEARTS)

        assertEquals(Card(Suit.HEARTS, Rank.SEVEN), card)
    }

    @Test
    fun `plays lowest card overall when no lead suit and no trump`() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.CLUBS, Rank.SEVEN),
            Card(Suit.DIAMONDS, Rank.ACE)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.TEN)))
        )

        val card = ai.selectCard(hand, trick, trumpSuit = null)

        assertEquals(Card(Suit.CLUBS, Rank.SEVEN), card)
    }

    @Test
    fun `plays lowest card when leading`() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.CLUBS, Rank.SEVEN),
            Card(Suit.DIAMONDS, Rank.ACE)
        )
        val trick = PartialTrick(emptyList())

        val card = ai.selectCard(hand, trick, trumpSuit = Suit.HEARTS)

        assertEquals(Card(Suit.CLUBS, Rank.SEVEN), card)
    }

    @Test
    fun `always passes in bidding`() {
        val bid = ai.selectBid(emptyList(), emptyList())
        assertTrue(bid.isPass)
        assertEquals(PlayerSeat.EAST, bid.player)
    }

    @Test
    fun `deterministic - same input produces same output`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.ACE),
            Card(Suit.HEARTS, Rank.SEVEN),
            Card(Suit.CLUBS, Rank.KING)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.SOUTH, Card(Suit.DIAMONDS, Rank.TEN)))
        )

        val card1 = ai.selectCard(hand, trick, trumpSuit = Suit.HEARTS)
        val card2 = ai.selectCard(hand, trick, trumpSuit = Suit.HEARTS)

        assertEquals(card1, card2)
    }
}
