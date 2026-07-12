package com.gusarik.engine.scoring

import com.gusarik.core.domain.model.Contract
import com.gusarik.core.domain.model.PlayerSeat

/**
 * Result of scoring a single round.
 */
data class RoundResult(
    val seat: PlayerSeat,
    val points: Int,
    val description: String
)

/**
 * Interface for scoring strategies.
 * Implement this to add new scoring systems (Сочи, Ленинград, etc.)
 */
interface ScoringStrategy {
    /**
     * Name of the scoring system.
     */
    val name: String

    /**
     * Score a round of play.
     *
     * @param contract The contract that was played (null for распасы)
     * @param trickCounts Number of tricks taken by each player
     * @param currentScores Cumulative scores before this round
     * @return Points awarded to each player this round
     */
    fun scoreRound(
        contract: Contract?,
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult>

    /**
     * Score a misere round.
     */
    fun scoreMisere(
        contractor: PlayerSeat,
        contractorTricks: Int,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult>

    /**
     * Score распасы (both players passed).
     */
    fun scorePassed(
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult>

    /**
     * Determine the winner of the game (if any).
     * Returns null if the game should continue.
     */
    fun determineWinner(
        scores: Map<PlayerSeat, Int>,
        roundNumber: Int
    ): PlayerSeat?
}
