package it.unibo.jakta.playground.gridworld

class Grid(
    val size: Int,
    val obstacles: Set<Position> = emptySet(),
) {
    fun isInBoundaries(position: Position): Boolean = position.x in 0 until size && position.y in 0 until size

    fun isObstacle(position: Position): Boolean = obstacles.contains(position)

    fun isObstacleInDirection(
        from: Position,
        direction: Direction,
    ): Boolean {
        val targetPosition = from.move(direction)
        return !isInBoundaries(targetPosition) || isObstacle(targetPosition)
    }
}
