package com.gusarik.engine.validation

import com.gusarik.core.domain.model.*

/**
 * Result of a validation check.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

/**
 * Validates all player actions against game rules.
 * This is the single source of truth for what moves are legal.
 */
object MoveValidator {

    /**
     * Validate a bid during the auction.
     */
    fun validateBid(
        bid: Bid,
        currentBids: List<Bid>,
        bidderSeat: PlayerSeat
    ): ValidationResult {
        // Must be the bidder's turn
        // (checked separately by TurnValidator)

        // Pass is always valid
        if (bid.isPass) return ValidationResult.Valid

        // Must be stronger than the current highest bid
        val highestBid = currentBids.filter { !it.isPass }.maxByOrNull { it.numericValue }
        if (highestBid != null && bid.numericValue <= highestBid.numericValue) {
            return ValidationResult.Invalid("Ставка должна быть выше текущей: $highestBid")
        }

        // Valid bid range
        if (bid.type == BidType.SUIT_BID || bid.type == BidType.NO_TRUMP) {
            val level = bid.level ?: return ValidationResult.Invalid("Уровень не указан")
            if (level !in 6..10) {
                return ValidationResult.Invalid("Уровень должен быть от 6 до 10")
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Validate a contract choice after winning the auction.
     */
    fun validateContract(
        contract: Contract,
        winningBid: Bid
    ): ValidationResult {
        if (contract.isMiser) return ValidationResult.Valid

        val contractBid = Bid.suitBid(contract.player, contract.level, contract.suit ?: Suit.SPADES)
        val contractNumeric = when {
            contract.suit != null -> contractBid.numericValue
            else -> Bid.noTrump(contract.player, contract.level).numericValue
        }

        if (contractNumeric < winningBid.numericValue) {
            return ValidationResult.Invalid("Контракт не может быть ниже выигранной ставки")
        }

        if (contract.level !in 6..10) {
            return ValidationResult.Invalid("Уровень контракта должен быть от 6 до 10")
        }

        return ValidationResult.Valid
    }

    /**
     * Validate a card play during a trick.
     */
    fun validateCardPlay(
        card: Card,
        hand: List<Card>,
        currentTrick: PartialTrick,
        trumpSuit: Suit?,
        contract: Contract?
    ): ValidationResult {
        // Card must be in the player's hand
        if (card !in hand) {
            return ValidationResult.Invalid("Карта не в руке игрока")
        }

        // If this is the first card of the trick, any card is valid
        if (currentTrick.isEmpty) return ValidationResult.Valid

        val leadSuit = currentTrick.leadSuit
            ?: return ValidationResult.Invalid("Ведущая масть не определена")

        // Must follow suit if possible
        val hasLeadSuit = hand.any { it.suit == leadSuit }
        if (hasLeadSuit && card.suit != leadSuit) {
            return ValidationResult.Invalid("Обязан положить масть: ${leadSuit.displayName}")
        }

        // If can't follow suit and has trump, must play trump
        if (!hasLeadSuit && trumpSuit != null) {
            val hasTrump = hand.any { it.suit == trumpSuit }
            if (hasTrump && card.suit != trumpSuit) {
                return ValidationResult.Invalid("Обязан положить козырь: ${trumpSuit.displayName}")
            }
        }

        // Misere: additional validation could go here (e.g., no trump allowed)
        // But standard rules still apply for following suit

        return ValidationResult.Valid
    }

    /**
     * Validate talon discard.
     */
    fun validateDiscard(
        cardsToDiscard: List<Card>,
        handWithTalon: List<Card>
    ): ValidationResult {
        if (cardsToDiscard.size != 2) {
            return ValidationResult.Invalid("Нужно сбросить ровно 2 карты")
        }

        for (card in cardsToDiscard) {
            if (card !in handWithTalon) {
                return ValidationResult.Invalid("Карта $card не в руке")
            }
        }

        // Check for duplicates
        if (cardsToDiscard.distinct().size != 2) {
            return ValidationResult.Invalid("Нельзя сбросить одну карту дважды")
        }

        return ValidationResult.Valid
    }
}
