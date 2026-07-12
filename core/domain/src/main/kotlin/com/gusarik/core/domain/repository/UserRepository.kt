package com.gusarik.core.domain.repository

import com.gusarik.core.domain.model.UserProfile
import com.gusarik.core.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * User profile repository interface.
 */
interface UserRepository {
    /**
     * Get user profile by ID.
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile>

    /**
     * Observe user profile changes.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?>

    /**
     * Create or update user profile.
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    /**
     * Update user statistics after a match.
     */
    suspend fun updateStats(userId: String, stats: UserStats): Result<Unit>

    /**
     * Upload avatar image.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String>
}
