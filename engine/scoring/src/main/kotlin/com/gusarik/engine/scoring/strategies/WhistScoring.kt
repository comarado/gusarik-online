package com.gusarik.engine.scoring.strategies

import com.gusarik.core.domain.model.Contract
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.engine.scoring.RoundResult
import com.gusarik.engine.scoring.ScoringStrategy

/**
 * "Висты" (Whist) scoring system — stub for future implementation.
 */
class WhistScoring : ScoringStrategy {

    override val name: String = "Висты"

    override fun scoreRound(
        contract: Contract?,
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        // TODO: Implement whist scoring
        // Fall back to bullet scoring for now
        return BulletScoring().scoreRound(contract, trickCounts, currentScores)
    }

    override fun scoreMisere(
        contractor: PlayerSeat,
        contractorTricks: Int,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        return BulletScoring().scoreMisere(contractor, contractorTricks, currentScores)
    }

    override fun scorePassed(
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        return BulletScoring().scorePassed(trickCounts, currentScores)
    }

    override fun determineWinner(
        scores: Map<PlayerSeat, Int>,
        roundNumber: Int
    ): PlayerSeat? = null
}
