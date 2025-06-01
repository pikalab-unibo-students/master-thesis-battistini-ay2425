package it.unibo.jakta.playground.gridworld

enum class Direction(
    val dx: Int,
    val dy: Int,
    val id: String,
) {
    NORTH(0, -1, "north"),
    SOUTH(0, 1, "south"),
    EAST(1, 0, "east"),
    WEST(-1, 0, "west"),
    NORTH_EAST(1, -1, "north_east"),
    NORTH_WEST(-1, -1, "north_west"),
    SOUTH_EAST(1, 1, "south_east"),
    SOUTH_WEST(-1, 1, "south_west"),
    HERE(0, 0, "here"),
    ;

    companion object {
        fun fromId(id: String): Direction? = entries.find { it.id == id }

        fun fromDeltas(
            dx: Int,
            dy: Int,
        ): Direction? =
            entries.find {
                it.dx == dx.coerceIn(-1, 1) && it.dy == dy.coerceIn(-1, 1)
            }
    }
}
