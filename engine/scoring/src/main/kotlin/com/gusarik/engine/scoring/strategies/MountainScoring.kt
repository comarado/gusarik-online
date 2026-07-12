package com.gusarik.engine.scoring.strategies

import com.gusarik.core.domain.model.Contract
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.engine.scoring.RoundResult
import com.gusarik.engine.scoring.ScoringStrategy

/**
 * "Гора" (Mountain) scoring system.
 *
 * Rules:
 * - Failed contracts accumulate points in the "mountain"
 * - When a player fulfills a contract, they take points from the mountain
 * - The mountain grows with each failed contract
 * - A fulfilled contract takes the accumulated mountain points
 */
class MountainScoring : ScoringStrategy {

    override val name: String = "Гора"

    // Mountain accumulates across rounds
    private var mountainPoints: Int = 0

    override fun scoreRound(
        contract: Contract?,
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        if (contract == null) return scorePassed(trickCounts, currentScores)
        if (contract.isMiser) return scoreMisere(contract.player, trickCounts[contract.player] ?: 0, currentScores)

        val contractor = contract.player
        val contractorTricks = trickCounts[contractor] ?: 0
        val required = contract.requiredTricks
        val basePoints = required * 10

        val results = mutableListOf<RoundResult>()

        if (contractorTricks >= required) {
            // Contract fulfilled — take from mountain
            val totalGain = basePoints + mountainPoints
            results.add(RoundResult(contractor, totalGain, "Контракт ${contract.displayName} сыгран (+$totalGain, гора: $mountainPoints)"))
            mountainPoints = 0

            for (seat in PlayerSeat.entries) {
                if (seat != contractor && seat != PlayerSeat.EAST) {
                    results.add(RoundResult(seat, -basePoints, "Проигрыш (-$basePoints)"))
                }
            }
            results.add(RoundResult(PlayerSeat.EAST, 0, "NPC"))
        } else {
            // Contract failed — add to mountain
            mountainPoints += basePoints
            results.add(RoundResult(contractor, -basePoints, "Контракт ${contract.displayName} не сыгран (-$basePoints), гора: $mountainPoints)"))

            for (seat in PlayerSeat.entries) {
                if (seat != contractor && seat != PlayerSeat.EAST) {
                    results.add(RoundResult(seat, basePoints, "Выигрыш (+$basePoints)"))
                }
            }
            results.add(RoundResult(PlayerSeat.EAST, 0, "NPC"))
        }

        return results
    }

    override fun scoreMisere(
        contractor: PlayerSeat,
        contractorTricks: Int,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        val results = mutableListOf<RoundResult>()
        val success = contractorTricks == 0
        val points = if (success) 100 + mountainPoints else -100

        if (success) {
            mountainPoints = 0
        }

        results.add(RoundResult(contractor, points, "Мизер ${if (success) "сыгран" else "не сыгран"} ($points)"))

        for (seat in PlayerSeat.entries) {
            if (seat != contractor && seat != PlayerSeat.EAST) {
                results.add(RoundResult(seat, -points, "Мизер оппонента (${-points})"))
            }
        }
        results.add(RoundResult(PlayerSeat.EAST, 0, "NPC"))

        return results
    }

    override fun scorePassed(
        trickCounts: Map<PlayerSeat, Int>,
        currentScores: Map<PlayerSeat, Int>
    ): List<RoundResult> {
        return PlayerSeat.entries.map { seat ->
            val tricks = trickCounts[seat] ?: 0
            val points = -tricks * 10
            RoundResult(seat, points, "Распасы: $tricks взяток ($points)")
        }
    }

    override fun determineWinner(
        scores: Map<PlayerSeat, Int>,
        roundNumber: Int
    ): PlayerSeat? = null

    /**
     * Reset the mountain (for new game).
     */
    fun resetMountain() {
        mountainPoints = 0
    }
}
