package com.gusarik.engine.scoring.strategies

import com.gusarik.core.domain.model.Contract
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.engine.scoring.RoundResult
import com.gusarik.engine.scoring.ScoringStrategy

/**
 * "Пуля" (Bullet) scoring system — the default Preferans scoring.
 *
 * Rules:
 * - Playing contract: contractor gets +10*level if they take >= level tricks,
 *   otherwise -10*level
 * - Misere: +100 if 0 tricks taken, -100 otherwise
 * - Распасы: -10 per trick taken
 * - Opponents get the negative of what the contractor gets
 */
class BulletScoring : ScoringStrategy {

    override val name: String = "Пуля"

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
            // Contract fulfilled
            results.add(RoundResult(contractor, basePoints, "Контракт ${contract.displayName} сыгран (+$basePoints)"))
            // Opponents lose
            for (seat in PlayerSeat.entries) {
                if (seat != contractor && seat != PlayerSeat.EAST) {
                    results.add(RoundResult(seat, -basePoints, "Проигрыш по контракту ${contract.displayName} (-$basePoints)"))
                }
            }
            // NPC doesn't get scored
            results.add(RoundResult(PlayerSeat.EAST, 0, "NPC"))
        } else {
            // Contract failed
            results.add(RoundResult(contractor, -basePoints, "Контракт ${contract.displayName} не сыгран (-$basePoints)"))
            for (seat in PlayerSeat.entries) {
                if (seat != contractor && seat != PlayerSeat.EAST) {
                    results.add(RoundResult(seat, basePoints, "Выигрыш по контракту ${contract.displayName} (+$basePoints)"))
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
        val points = if (success) 100 else -100

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
    ): PlayerSeat? {
        // In bullet scoring, game typically ends after a set number of rounds
        // or when a player reaches a target score. Here we use round limit.
        return null // No automatic winner; game ends when players decide
    }

    companion object {
        const val DEFAULT_ROUNDS = 12
    }
}
