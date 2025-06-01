package it.unibo.jakta.playground.gridworld

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.engine.logging.loggers.MasLogger
import it.unibo.jakta.agents.bdi.engine.messages.MessageQueue
import it.unibo.jakta.agents.bdi.engine.perception.Perception
import it.unibo.jakta.playground.gridworld.logging.ObjectReachedEvent

class GridWorld(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    override var perception: Perception = Perception.empty(),
    data: Map<String, Any> = defaultData,
    override val logger: MasLogger? = null,
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data, logger) {
    private val grid: Grid = createGrid()
    private val beliefFactory = BeliefFactory()

    init {
        perception = Perception.of(getPercepts())
    }

    private fun createGrid(): Grid {
        val gridSize = data.gridSize() ?: DEFAULT_GRID_SIZE
        val obstacles =
            data
                .obstacles()
                ?.map { Position(it.x, it.y) }
                ?.toSet() ?: defaultObstacles
                .map {
                    Position(it.x, it.y)
                }.toSet()

        return Grid(gridSize, obstacles)
    }

    private fun getCurrentState(): GridWorldState? {
        val agentPosition = data.currentPosition()?.let { Position(it.x, it.y) } ?: return null
        val objectsPosition =
            (data.objects() ?: defaultObjects).mapValues { (_, cell) ->
                Position(cell.x, cell.y)
            }

        return GridWorldState(
            grid = grid,
            agentPosition = agentPosition,
            objectsPosition = objectsPosition,
            availableDirections = Direction.entries.toSet(),
        )
    }

    override fun updateData(newData: Map<String, Any>): GridWorld =
        newData.directionToMove()?.let { dirId ->
            Direction.fromId(dirId)?.let { direction ->
                moveRobot(direction)
            } ?: this
        } ?: this

    private fun moveRobot(direction: Direction): GridWorld {
        val currentState = getCurrentState() ?: return this
        val newPosition = currentState.agentPosition.move(direction)

        return if (grid.isInBoundaries(newPosition) && !grid.isObstacle(newPosition)) {
            val newPosition = Cell(newPosition.x, newPosition.y)
            data.objects()?.let { objects ->
                objects.forEach { (objectName, objectCell) ->
                    if (objectCell == newPosition) {
                        logger?.log { ObjectReachedEvent(objectName) }
                    }
                }
            }
            this.copy(data = data.updateCurrentPosition(newPosition))
        } else {
            this
        }
    }

    override fun percept(): BeliefBase {
        perception = Perception.of(getPercepts())
        return super.percept()
    }

    private fun getPercepts(): List<Belief> {
        val currentState = getCurrentState() ?: return emptyList()

        val directionBeliefs = beliefFactory.createDirectionBeliefs(currentState)
        val objectBeliefs = beliefFactory.createObjectBeliefs(currentState)
        val obstacleBeliefs = beliefFactory.createObstacleBeliefs(grid, currentState)
        val thereIsBeliefs = beliefFactory.createThereIsBeliefs(currentState)

        return directionBeliefs + objectBeliefs + obstacleBeliefs + thereIsBeliefs
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
        logger: MasLogger?,
    ): GridWorld =
        GridWorld(
            agentIDs,
            externalActions,
            messageBoxes,
            perception,
            data,
            logger,
        )

    companion object {
        @Suppress("UNCHECKED_CAST")
        internal fun Map<String, Any>.obstacles() = this["obstacles"] as? Set<Cell>

        internal fun Map<String, Any>.directionToMove() = this["directionToMove"] as? String

        internal fun Map<String, Any>.gridSize() = this["gridSize"] as? Int

        internal fun Map<String, Any>.updateCurrentPosition(p: Cell) = this.plus("currentPosition" to p)

        internal fun Map<String, Any>.currentPosition() = this["currentPosition"] as? Cell

        @Suppress("UNCHECKED_CAST")
        internal fun Map<String, Any>.directions() = this["directions"] as? Set<String>

        @Suppress("UNCHECKED_CAST")
        internal fun Map<String, Any>.objects() = this["objects"] as? Map<String, Cell>

        internal val defaultObjects =
            mapOf(
                "rock" to Cell(1, 0),
                "home" to Cell(DEFAULT_GRID_SIZE - 1, DEFAULT_GRID_SIZE - 1),
            )

        internal val defaultObstacles =
            setOf(
                Cell(2, 3), // south
                Cell(1, 3), // south-west
                Cell(3, 3), // south-east
            )

        internal val defaultDirections =
            setOf(
                "north",
                "south",
                "east",
                "west",
                "north_east",
                "north_west",
                "south_east",
                "south_west",
            )

        internal const val DEFAULT_GRID_SIZE = 5

        internal val DEFAULT_START_POSITION = Cell(2, 2)

        internal val defaultData =
            mapOf(
                "directions" to defaultDirections,
                "objects" to defaultObjects,
                "obstacles" to defaultObstacles,
                "currentPosition" to DEFAULT_START_POSITION,
                "gridSize" to DEFAULT_GRID_SIZE,
            )
    }
}
