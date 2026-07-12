package com.gusarik.core.domain.model

/**
 * Card suit.
 * Ordering follows Preferans seniority: SPADES < CLUBS < DIAMONDS < HEARTS
 */
enum class Suit(val symbol: String, val displayName: String) {
    SPADES("♠", "Пики"),
    CLUBS("♣", "Трефы"),
    DIAMONDS("♦", "Бубны"),
    HEARTS("♥", "Черви");

    companion object {
        fun fromSymbol(symbol: String): Suit? = entries.find { it.symbol == symbol }
    }
}

/**
 * Card rank.
 * Ordering: SEVEN < EIGHT < NINE < TEN < JACK < QUEEN < KING < ACE
 */
enum class Rank(val symbol: String, val displayName: String, val value: Int) {
    SEVEN("7", "Семёрка", 7),
    EIGHT("8", "Восьмёрка", 8),
    NINE("9", "Девятка", 9),
    TEN("10", "Десятка", 10),
    JACK("В", "Валет", 11),
    QUEEN("Д", "Дама", 12),
    KING("К", "Король", 13),
    ACE("Т", "Туз", 14);

    companion object {
        fun fromSymbol(symbol: String): Rank? = entries.find {
            it.symbol.equals(symbol, ignoreCase = true)
        }
    }
}

/**
 * A playing card with suit and rank.
 */
data class Card(
    val suit: Suit,
    val rank: Rank
) : Comparable<Card> {

    /**
     * Compare by rank within the same suit context.
     * For cross-suit comparison, use a comparator with trump awareness.
     */
    override fun compareTo(other: Card): Int {
        return this.rank.value.compareTo(other.rank.value)
    }

    override fun toString(): String = "${rank.symbol}${suit.symbol}"

    companion object {
        /**
         * Create a full 32-card Preferans deck.
         */
        fun fullDeck(): List<Card> {
            return Suit.entries.flatMap { suit ->
                Rank.entries.map { rank -> Card(suit, rank) }
            }
        }
    }
}

/**
 * Special "no trump" suit designation for BК (Без Козыря) contracts.
 */
object NoTrump {
    const val DISPLAY = "БК"
}
