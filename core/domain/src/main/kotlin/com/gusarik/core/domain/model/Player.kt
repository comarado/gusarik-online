package com.gusarik.core.domain.model

/**
 * Player seat at the table.
 * SOUTH = Real player 1 (bottom of screen)
 * NORTH = Real player 2 (top of screen)
 * EAST  = Virtual/NPC player (right side)
 */
enum class PlayerSeat(val index: Int, val displayName: String) {
    SOUTH(0, "Юг"),
    NORTH(1, "Север"),
    EAST(2, "Восток");

    /**
     * Next seat in clockwise order.
     */
    fun next(): PlayerSeat = when (this) {
        SOUTH -> EAST
        EAST -> NORTH
        NORTH -> SOUTH
    }

    /**
     * Previous seat (counter-clockwise).
     */
    fun previous(): PlayerSeat = when (this) {
        SOUTH -> NORTH
        NORTH -> EAST
        EAST -> SOUTH
    }

    val isReal: Boolean get() = this != EAST
    val isNpc: Boolean get() = this == EAST
}

/**
 * Player data.
 */
data class Player(
    val id: String,
    val seat: PlayerSeat,
    val nickname: String,
    val avatarUrl: String? = null
) {
    val isNpc: Boolean get() = seat.isNpc

    companion object {
        /**
         * Create the virtual NPC player.
         */
        fun npc(): Player = Player(
            id = "npc",
            seat = PlayerSeat.EAST,
            nickname = "Виртуальный"
        )
    }
}
