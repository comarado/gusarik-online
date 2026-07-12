package com.gusarik.engine.game

import com.gusarik.core.domain.model.*
import com.gusarik.engine.game.playing.TrickResolver
import org.junit.Assert.*
import org.junit.Test

class TrickResolverTest {

    @Test
    fun `highest card of lead suit wins when no trump`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.SEVEN)),
                TrickCard(PlayerSeat.EAST, Card(Suit.SPADES, Rank.KING)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.SPADES, Rank.TEN))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = null)
        assertEquals(PlayerSeat.EAST, winner)
    }

    @Test
    fun `trump beats lead suit`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.ACE)),
                TrickCard(PlayerSeat.EAST, Card(Suit.HEARTS, Rank.SEVEN)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.SPADES, Rank.KING))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = Suit.HEARTS)
        assertEquals(PlayerSeat.EAST, winner)
    }

    @Test
    fun `higher trump beats lower trump`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.HEARTS, Rank.SEVEN)),
                TrickCard(PlayerSeat.EAST, Card(Suit.HEARTS, Rank.KING)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.HEARTS, Rank.TEN))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = Suit.HEARTS)
        assertEquals(PlayerSeat.EAST, winner)
    }

    @Test
    fun `off-suit cards do not beat lead suit when no trump`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.SPADES, Rank.SEVEN)),
                TrickCard(PlayerSeat.EAST, Card(Suit.HEARTS, Rank.ACE)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.CLUBS, Rank.ACE))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = null)
        assertEquals(PlayerSeat.SOUTH, winner) // Only spade is the 7
    }

    @Test
    fun `first card wins when all follow suit with lower cards`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.DIAMONDS, Rank.ACE)),
                TrickCard(PlayerSeat.EAST, Card(Suit.DIAMONDS, Rank.SEVEN)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.DIAMONDS, Rank.EIGHT))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = null)
        assertEquals(PlayerSeat.SOUTH, winner)
    }

    @Test
    fun `trick with mixed suits and no trump - lead suit wins`() {
        val trick = PartialTrick(
            cards = listOf(
                TrickCard(PlayerSeat.SOUTH, Card(Suit.CLUBS, Rank.SEVEN)),
                TrickCard(PlayerSeat.EAST, Card(Suit.DIAMONDS, Rank.ACE)),
                TrickCard(PlayerSeat.NORTH, Card(Suit.CLUBS, Rank.TEN))
            )
        )

        val winner = TrickResolver.resolveTrick(trick, trumpSuit = Suit.HEARTS)
        assertEquals(PlayerSeat.NORTH, winner) // Highest club (lead suit) wins
    }
}
