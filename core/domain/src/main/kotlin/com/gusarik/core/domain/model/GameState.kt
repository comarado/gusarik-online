package com.gusarik.core.domain.model

/**
 * Phases of a game round.
 */
enum class GamePhase {
    LOBBY,          // Waiting for players
    DEALING,        // Cards being dealt
    BIDDING,        // Auction in progress
    PASSED,         // Both passed → распасы
    WON_BID,        // Auction won, choosing contract
    TALON,          // Winner sees talon, discards
    PLAYING,        // Trick-taking in progress
    SCORING,        // Round finished, showing scores
    FINISHED,       // Game over
    NEXT_DEAL       // Transitioning to next deal
}

/**
 * Complete state of a game.
 * This is the single source of truth, shared between players via Firestore.
 */
data class GameState(
    val gameId: String,
    val phase: GamePhase,
    val players: Map<PlayerSeat, Player>,
    val hands: Map<PlayerSeat, List<Card>>,
    val talon: List<Card> = emptyList(),
    val currentTrick: PartialTrick = PartialTrick(emptyList()),
    val completedTricks: List<Trick> = emptyList(),
    val trickCounts: Map<PlayerSeat, Int> = mapOf(
        PlayerSeat.SOUTH to 0,
        PlayerSeat.NORTH to 0,
        PlayerSeat.EAST to 0
    ),
    val bids: List<Bid> = emptyList(),
    val currentBidder: PlayerSeat? = null,
    val contract: Contract? = null,
    val currentPlayer: PlayerSeat? = null,
    val trumpSuit: Suit? = null,
    val scores: Map<PlayerSeat, Int> = mapOf(
        PlayerSeat.SOUTH to 0,
        PlayerSeat.NORTH to 0,
        PlayerSeat.EAST to 0
    ),
    val roundNumber: Int = 1,
    val roundScores: List<RoundScore> = emptyList(),
    val dealer: PlayerSeat = PlayerSeat.SOUTH,
    val turnDeadline: Long? = null,
    val lastAction: GameAction? = null,
    val isDisconnection: Map<PlayerSeat, Boolean> = mapOf(
        PlayerSeat.SOUTH to false,
        PlayerSeat.NORTH to false
    )
) {
    /**
     * Get the hand for a specific seat.
     */
    fun handOf(seat: PlayerSeat): List<Card> = hands[seat] ?: emptyList()

    /**
     * Get the player at a specific seat.
     */
    fun playerAt(seat: PlayerSeat): Player? = players[seat]

    /**
     * Is it the given player's turn?
     */
    fun isTurnOf(seat: PlayerSeat): Boolean = currentPlayer == seat

    /**
     * Is the game in the dealing/bidding phase?
     */
    val isPrePlay: Boolean
        get() = phase in setOf(GamePhase.DEALING, GamePhase.BIDDING, GamePhase.WON_BID, GamePhase.TALON)

    /**
     * Is the game actively being played?
     */
    val isActive: Boolean
        get() = phase in setOf(GamePhase.PLAYING, GamePhase.BIDDING, GamePhase.WON_BID, GamePhase.TALON)

    /**
     * Is this a misere contract?
     */
    val isMisere: Boolean get() = contract?.isMiser == true

    /**
     * Is this a passed hand (распасы)?
     */
    val isPassed: Boolean get() = phase == GamePhase.PASSED

    /**
     * Number of tricks remaining in the current round.
     */
    val tricksRemaining: Int
        get() = 10 - completedTricks.size

    /**
     * Total tricks completed.
     */
    val totalTricks: Int get() = completedTricks.size
}

/**
 * Score for a single round.
 */
data class RoundScore(
    val roundNumber: Int,
    val contract: Contract?,
    val trickCounts: Map<PlayerSeat, Int>,
    val roundPoints: Map<PlayerSeat, Int>,
    val cumulativeScores: Map<PlayerSeat, Int>
)

/**
 * Summary of a completed match.
 */
data class MatchResult(
    val gameId: String,
    val player1Id: String,
    val player2Id: String,
    val player1Nickname: String,
    val player2Nickname: String,
    val finalScores: Map<PlayerSeat, Int>,
    val roundScores: List<RoundScore>,
    val winnerSeat: PlayerSeat?,
    val startedAt: Long,
    val finishedAt: Long,
    val totalRounds: Int
)
