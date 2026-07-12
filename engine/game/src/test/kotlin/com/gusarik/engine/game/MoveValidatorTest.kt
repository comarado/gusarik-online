package com.gusarik.engine.game

import com.gusarik.core.domain.model.*
import com.gusarik.engine.validation.MoveValidator
import com.gusarik.engine.validation.ValidationResult
import org.junit.Assert.*
import org.junit.Test

class MoveValidatorTest {

    // === Card Play Validation ===

    @Test
    fun `can play any card when leading`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.ACE),
            Card(Suit.CLUBS, Rank.KING)
        )
        val trick = PartialTrick(emptyList())

        val result = MoveValidator.validateCardPlay(
            card = hand[0],
            hand = hand,
            currentTrick = trick,
            trumpSuit = Suit.HEARTS,
            contract = null
        )

        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `must follow suit if possible`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.SPADES, Rank.ACE),
            Card(Suit.HEARTS, Rank.KING)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.NORTH, Card(Suit.SPADES, Rank.TEN)))
        )

        // Try to play heart when we have spades
        val result = MoveValidator.validateCardPlay(
            card = hand[2], // heart
            hand = hand,
            currentTrick = trick,
            trumpSuit = Suit.HEARTS,
            contract = null
        )

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `can play off-suit when cannot follow`() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.SEVEN),
            Card(Suit.CLUBS, Rank.KING)
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.NORTH, Card(Suit.SPADES, Rank.TEN)))
        )

        val result = MoveValidator.validateCardPlay(
            card = hand[0], // heart
            hand = hand,
            currentTrick = trick,
            trumpSuit = null,
            contract = null
        )

        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `must play trump if cannot follow suit and has trump`() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.SEVEN),  // trump
            Card(Suit.CLUBS, Rank.KING)      // off-suit
        )
        val trick = PartialTrick(
            listOf(TrickCard(PlayerSeat.NORTH, Card(Suit.SPADES, Rank.TEN)))
        )

        // Try to play club when we have trump
        val result = MoveValidator.validateCardPlay(
            card = hand[1], // club
            hand = hand,
            currentTrick = trick,
            trumpSuit = Suit.HEARTS,
            contract = null
        )

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `cannot play card not in hand`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN)
        )
        val trick = PartialTrick(emptyList())

        val result = MoveValidator.validateCardPlay(
            card = Card(Suit.HEARTS, Rank.ACE),
            hand = hand,
            currentTrick = trick,
            trumpSuit = null,
            contract = null
        )

        assertTrue(result is ValidationResult.Invalid)
    }

    // === Bid Validation ===

    @Test
    fun `pass is always valid bid`() {
        val result = MoveValidator.validateBid(
            bid = Bid.pass(PlayerSeat.SOUTH),
            currentBids = emptyList(),
            bidderSeat = PlayerSeat.SOUTH
        )
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `bid must be higher than current highest`() {
        val currentBids = listOf(
            Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.HEARTS)
        )

        val result = MoveValidator.validateBid(
            bid = Bid.suitBid(PlayerSeat.NORTH, 6, Suit.SPADES),
            currentBids = currentBids,
            bidderSeat = PlayerSeat.NORTH
        )

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `valid higher bid is accepted`() {
        val currentBids = listOf(
            Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.HEARTS)
        )

        val result = MoveValidator.validateBid(
            bid = Bid.suitBid(PlayerSeat.NORTH, 6, Suit.NO_TRUMP ?: Suit.SPADES),
            currentBids = currentBids,
            bidderSeat = PlayerSeat.NORTH
        )

        // 6БК > 6♥
        assertTrue(result is ValidationResult.Valid)
    }

    // === Contract Validation ===

    @Test
    fun `contract cannot be below winning bid`() {
        val winningBid = Bid.suitBid(PlayerSeat.SOUTH, 7, Suit.HEARTS)
        val contract = Contract(PlayerSeat.SOUTH, 6, Suit.SPADES)

        val result = MoveValidator.validateContract(contract, winningBid)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `miser is always valid contract`() {
        val winningBid = Bid.suitBid(PlayerSeat.SOUTH, 6, Suit.SPADES)
        val contract = Contract.miser(PlayerSeat.SOUTH)

        val result = MoveValidator.validateContract(contract, winningBid)
        assertTrue(result is ValidationResult.Valid)
    }

    // === Discard Validation ===

    @Test
    fun `must discard exactly 2 cards`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.ACE)
        )

        val result = MoveValidator.validateDiscard(
            cardsToDiscard = listOf(hand[0]),
            handWithTalon = hand
        )

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `cannot discard card not in hand`() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.ACE)
        )

        val result = MoveValidator.validateDiscard(
            cardsToDiscard = listOf(
                Card(Suit.CLUBS, Rank.KING),
                Card(Suit.DIAMONDS, Rank.QUEEN)
            ),
            handWithTalon = hand
        )

        assertTrue(result is ValidationResult.Invalid)
    }
}
