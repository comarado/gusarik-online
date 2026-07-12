package com.gusarik.engine.game

import com.gusarik.core.domain.model.Contract
import com.gusarik.core.domain.model.PlayerSeat
import com.gusarik.core.domain.model.Suit
import com.gusarik.engine.scoring.strategies.BulletScoring
import com.gusarik.engine.scoring.strategies.MountainScoring
import org.junit.Assert.*
import org.junit.Test

class ScoringTest {

    private val bulletScoring = BulletScoring()

    // === Bullet Scoring ===

    @Test
    fun `contract fulfilled gives positive points to contractor`() {
        val contract = Contract(PlayerSeat.SOUTH, 6, Suit.SPADES)
        val trickCounts = mapOf(
            PlayerSeat.SOUTH to 6,
            PlayerSeat.NORTH to 3,
            PlayerSeat.EAST to 1
        )

        val results = bulletScoring.scoreRound(contract, trickCounts, emptyMap())

        val southResult = results.find { it.seat == PlayerSeat.SOUTH }!!
        assertEquals(60, southResult.points) // 6 * 10
    }

    @Test
    fun `contract failed gives negative points to contractor`() {
        val contract = Contract(PlayerSeat.SOUTH, 7, Suit.HEARTS)
        val trickCounts = mapOf(
            PlayerSeat.SOUTH to 5,
            PlayerSeat.NORTH to 4,
            PlayerSeat.EAST to 1
        )

        val results = bulletScoring.scoreRound(contract, trickCounts, emptyMap())

        val southResult = results.find { it.seat == PlayerSeat.SOUTH }!!
        assertEquals(-70, southResult.points) // -7 * 10
    }

    @Test
    fun `misere success gives 100 points`() {
        val results = bulletScoring.scoreMisere(
            contractor = PlayerSeat.SOUTH,
            contractorTricks = 0,
            currentScores = emptyMap()
        )

        val southResult = results.find { it.seat == PlayerSeat.SOUTH }!!
        assertEquals(100, southResult.points)
    }

    @Test
    fun `misere failure gives -100 points`() {
        val results = bulletScoring.scoreMisere(
            contractor = PlayerSeat.SOUTH,
            contractorTricks = 1,
            currentScores = emptyMap()
        )

        val southResult = results.find { it.seat == PlayerSeat.SOUTH }!!
        assertEquals(-100, southResult.points)
    }

    @Test
    fun `passed hand deducts 10 per trick`() {
        val trickCounts = mapOf(
            PlayerSeat.SOUTH to 3,
            PlayerSeat.NORTH to 5,
            PlayerSeat.EAST to 2
        )

        val results = bulletScoring.scorePassed(trickCounts, emptyMap())

        val southResult = results.find { it.seat == PlayerSeat.SOUTH }!!
        val northResult = results.find { it.seat == PlayerSeat.NORTH }!!
        val eastResult = results.find { it.seat == PlayerSeat.EAST }!!

        assertEquals(-30, southResult.points)
        assertEquals(-50, northResult.points)
        assertEquals(-20, eastResult.points)
    }

    // === Mountain Scoring ===

    @Test
    fun `mountain accumulates on failed contracts`() {
        val mountainScoring = MountainScoring()

        val contract = Contract(PlayerSeat.SOUTH, 6, Suit.SPADES)
        val trickCounts = mapOf(
            PlayerSeat.SOUTH to 4,
            PlayerSeat.NORTH to 5,
            PlayerSeat.EAST to 1
        )

        mountainScoring.scoreRound(contract, trickCounts, emptyMap())

        // Mountain should now have 60 points (6 * 10)
        // Second failed contract should add more
        val contract2 = Contract(PlayerSeat.NORTH, 7, Suit.HEARTS)
        val trickCounts2 = mapOf(
            PlayerSeat.SOUTH to 5,
            PlayerSeat.NORTH to 4,
            PlayerSeat.EAST to 1
        )

        val results = mountainScoring.scoreRound(contract2, trickCounts2, emptyMap())

        val northResult = results.find { it.seat == PlayerSeat.NORTH }!!
        // North failed 7-level contract: -70 points
        assertEquals(-70, northResult.points)
    }
}
