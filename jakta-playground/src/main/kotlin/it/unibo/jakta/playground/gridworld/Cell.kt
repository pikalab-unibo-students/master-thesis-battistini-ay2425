package it.unibo.jakta.playground.gridworld

data class Cell(
    val x: Int,
    val y: Int,
) {
    fun asPosition(): Position = Position(x, y)
}
