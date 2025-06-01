package it.unibo.jakta.playground.gridworld

data class GridWorldState(
    val grid: Grid,
    val agentPosition: Position,
    val objectsPosition: Map<String, Position>,
    val availableDirections: Set<Direction>,
)
