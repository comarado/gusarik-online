package com.gusarik.core.domain.model

/**
 * User profile data.
 */
data class UserProfile(
    val id: String,
    val nickname: String,
    val email: String,
    val avatarUrl: String? = null,
    val stats: UserStats = UserStats(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

/**
 * Player statistics.
 */
data class UserStats(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalTricks: Int = 0,
    val favoriteContract: String? = null,
    val avgGameDurationSeconds: Long = 0,
    val totalScore: Int = 0
) {
    val winRate: Float
        get() = if (gamesPlayed > 0) wins.toFloat() / gamesPlayed else 0f

    val winRatePercent: Int
        get() = (winRate * 100).toInt()
}

/**
 * Scoring system type.
 */
enum class ScoringSystem(val displayName: String) {
    BULLET("Пуля"),
    MOUNTAIN("Гора"),
    WHIST("Висты"),
    SOCHI("Сочи"),
    LENINGRAD("Ленинград"),
    ROSTOV("Ростов"),
    PETERSBURG("Питер")
}

/**
 * Game settings configurable by players.
 */
data class GameSettings(
    val scoringSystem: ScoringSystem = ScoringSystem.BULLET,
    val turnTimeLimitSeconds: Int = 60,
    val disconnectionTimeoutSeconds: Int = 120,
    val allowChat: Boolean = true,
    val autoPlayOnTimeout: Boolean = true
)

/**
 * Room status.
 */
enum class RoomStatus {
    WAITING,
    PLAYING,
    FINISHED,
    ABANDONED
}

/**
 * Room data for matchmaking.
 */
data class Room(
    val code: String,
    val hostId: String,
    val guestId: String? = null,
    val status: RoomStatus = RoomStatus.WAITING,
    val settings: GameSettings = GameSettings(),
    val createdAt: Long = System.currentTimeMillis(),
    val gameId: String? = null
)
