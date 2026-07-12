package com.gusarik.core.domain.repository

import com.gusarik.core.domain.model.MatchResult
import kotlinx.coroutines.flow.Flow

/**
 * Match history repository interface.
 */
interface MatchRepository {
    /**
     * Save a completed match.
     */
    suspend fun saveMatch(match: MatchResult): Result<Unit>

    /**
     * Get match history for a user.
     */
    suspend fun getMatchHistory(userId: String): Result<List<MatchResult>>

    /**
     * Observe match history changes.
     */
    fun observeMatchHistory(userId: String): Flow<List<MatchResult>>

    /**
     * Get a specific match by ID.
     */
    suspend fun getMatch(matchId: String): Result<MatchResult>
}
