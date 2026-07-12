package com.gusarik.engine.game

import com.gusarik.core.domain.model.Card
import com.gusarik.core.domain.model.Rank
import com.gusarik.core.domain.model.Suit
import com.gusarik.engine.game.dealing.Deck
import org.junit.Assert.*
import org.junit.Test

class DeckTest {

    @Test
    fun `full deck has 32 cards`() {
        val deck = Deck()
        assertEquals(32, deck.size)
    }

    @Test
    fun `full deck has 8 cards per suit`() {
        val cards = Card.fullDeck()
        for (suit in Suit.entries) {
            val suitCards = cards.filter { it.suit == suit }
            assertEquals("Suit $suit should have 8 cards", 8, suitCards.size)
        }
    }

    @Test
    fun `deal produces correct distribution`() {
        val deck = Deck()
        deck.shuffle(seed = 42)
        val result = deck.deal()

        // Each hand should have 10 cards
        assertEquals(10, result.hand1.size)
        assertEquals(10, result.hand2.size)
        assertEquals(10, result.hand3.size)

        // Talon should have 2 cards
        assertEquals(2, result.talon.size)

        // All cards should be unique
        val allCards = result.hand1 + result.hand2 + result.hand3 + result.talon
        assertEquals(32, allCards.distinct().size)
    }

    @Test
    fun `shuffle with same seed produces same result`() {
        val deck1 = Deck()
        deck1.shuffle(seed = 123)
        val result1 = deck1.deal()

        val deck2 = Deck()
        deck2.shuffle(seed = 123)
        val result2 = deck2.deal()

        assertEquals(result1.hand1, result2.hand1)
        assertEquals(result1.hand2, result2.hand2)
        assertEquals(result1.hand3, result2.hand3)
        assertEquals(result1.talon, result2.talon)
    }

    @Test
    fun `shuffle with different seeds produces different results`() {
        val deck1 = Deck()
        deck1.shuffle(seed = 1)
        val result1 = deck1.deal()

        val deck2 = Deck()
        deck2.shuffle(seed = 2)
        val result2 = deck2.deal()

        // Very unlikely to be the same with different seeds
        assertNotEquals(result1.hand1, result2.hand1)
    }

    @Test
    fun `hands are sorted by suit then rank`() {
        val deck = Deck()
        deck.shuffle(seed = 42)
        val result = deck.deal()

        for (hand in listOf(result.hand1, result.hand2, result.hand3)) {
            for (i in 0 until hand.size - 1) {
                val current = hand[i]
                val next = hand[i + 1]
                assertTrue(
                    "Hand should be sorted: ${current} before ${next}",
                    current.suit.ordinal < next.suit.ordinal ||
                            (current.suit == next.suit && current.rank.value <= next.rank.value)
                )
            }
        }
    }
}
