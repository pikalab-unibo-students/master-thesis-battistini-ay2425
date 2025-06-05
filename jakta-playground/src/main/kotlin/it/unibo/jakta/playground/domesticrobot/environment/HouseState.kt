package it.unibo.jakta.playground.domesticrobot.environment

import it.unibo.jakta.playground.domesticrobot.environment.HouseEnvironment.Companion.DEFAULT_GRID_SIZE
import it.unibo.jakta.playground.domesticrobot.environment.HouseEnvironment.Companion.DEFAULT_START_POSITION
import it.unibo.jakta.playground.domesticrobot.environment.HouseEnvironment.Companion.defaultObjects
import it.unibo.jakta.playground.gridworld.Cell
import it.unibo.jakta.playground.gridworld.Direction
import it.unibo.jakta.playground.gridworld.Grid
import it.unibo.jakta.playground.gridworld.Position

data class HouseState(
    val fridgeOpen: Boolean = false,
    val carryingBeer: Boolean = false,
    val sipCount: Int = START_SIP_COUNT,
    val availableBeers: Int = START_BEER_COUNT,
    val gridSize: Int = DEFAULT_GRID_SIZE,
    val robotPosition: Position = DEFAULT_START_POSITION,
    val grid: Grid = Grid(gridSize),
    val objects: Map<String, Cell> = defaultObjects,
) {
    fun openFridge() = copy(fridgeOpen = true)

    fun closeFridge() = copy(fridgeOpen = false)

    fun getBeer() =
        if (fridgeOpen && availableBeers > 0 && !carryingBeer) {
            copy(
                availableBeers = availableBeers - 1,
                carryingBeer = true,
            )
        } else {
            null
        }

    fun addBeer(numBeers: Int = 1) = copy(availableBeers = availableBeers + numBeers)

    fun moveTowards(objectToReach: String): HouseState? {
        val targetPosition = objects[objectToReach]?.asPosition()
        return if (targetPosition == null || robotPosition.isOn(targetPosition)) {
            null
        } else {
            val direction = robotPosition.directionTo(targetPosition)
            direction?.let { dir ->
                val newRobotPosition = moveRobot(robotPosition, dir)
                newRobotPosition?.let { copy(robotPosition = it) }
            }
        }
    }

    private fun moveRobot(
        currentPosition: Position,
        direction: Direction,
    ): Position? {
        val newPosition = currentPosition.move(direction)
        return if (grid.isInBoundaries(newPosition) && !grid.isObstacle(newPosition)) {
            Position(newPosition.x, newPosition.y)
        } else {
            null
        }
    }

    fun handInBeer() =
        if (carryingBeer) {
            copy(
                sipCount = MAX_SIP_COUNT,
                carryingBeer = false,
            )
        } else {
            null
        }

    fun sipBeer() =
        if (sipCount > 0) {
            copy(sipCount = sipCount - 1)
        } else {
            null
        }

    companion object {
        const val MAX_SIP_COUNT = 3
        const val START_SIP_COUNT = 0
        const val START_BEER_COUNT = 1
    }
}
