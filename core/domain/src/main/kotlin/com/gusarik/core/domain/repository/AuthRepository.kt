package com.gusarik.core.domain.repository

import com.gusarik.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Authentication repository interface.
 */
interface AuthRepository {
    /**
     * Currently authenticated user, or null.
     */
    val currentUser: Flow<UserProfile?>

    /**
     * Check if user is authenticated.
     */
    fun isAuthenticated(): Boolean

    /**
     * Get current user ID.
     */
    fun getCurrentUserId(): String?

    /**
     * Sign in with Google.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>

    /**
     * Sign in with email and password.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>

    /**
     * Register with email and password.
     */
    suspend fun signUpWithEmail(email: String, password: String, nickname: String): Result<UserProfile>

    /**
     * Sign out.
     */
    suspend fun signOut()
}
