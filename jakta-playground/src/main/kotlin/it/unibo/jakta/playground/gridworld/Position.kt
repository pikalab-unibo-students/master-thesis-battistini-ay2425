package it.unibo.jakta.playground.gridworld

import kotlin.math.abs

data class Position(
    val x: Int,
    val y: Int,
) {
    fun move(direction: Direction): Position = Position(x + direction.dx, y + direction.dy)

    fun directionTo(other: Position): Direction? {
        val dx = (other.x - x).coerceIn(-1, 1)
        val dy = (other.y - y).coerceIn(-1, 1)
        return Direction.fromDeltas(dx, dy)
    }

    fun isAdjacentTo(other: Position): Boolean {
        val dx = other.x - x
        val dy = other.y - y
        return abs(dx) <= 1 && abs(dy) <= 1 && !(dx == 0 && dy == 0)
    }

    fun isOn(other: Position): Boolean = x == other.x && y == other.y
}
