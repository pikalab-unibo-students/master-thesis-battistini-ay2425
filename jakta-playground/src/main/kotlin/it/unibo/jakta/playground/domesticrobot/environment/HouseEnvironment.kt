package it.unibo.jakta.playground.domesticrobot.environment

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.Jakta.capitalize
import it.unibo.jakta.agents.bdi.engine.JaktaParser.tangleStruct
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.engine.logging.loggers.MasLogger
import it.unibo.jakta.agents.bdi.engine.messages.MessageQueue
import it.unibo.jakta.agents.bdi.engine.perception.Perception
import it.unibo.jakta.playground.gridworld.Cell
import it.unibo.jakta.playground.gridworld.Position
import it.unibo.tuprolog.core.Atom
import java.lang.Thread.sleep

class HouseEnvironment(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    override var perception: Perception = Perception.empty(),
    data: Map<String, Any> = defaultHouseData,
    override val logger: MasLogger? = null,
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data, logger) {
    private val beliefFactory = HouseBeliefFactory()

    init {
        perception = Perception.of(getPercepts())
    }

    private fun getPercepts(): List<Belief> {
        val model = data["model"] as? HouseState
        if (model != null) {
            val hasOwnerBeer = beliefFactory.createHasOwnerBelief(model)
            val robotLocation = beliefFactory.createRobotLocationBelief(model)
            val beerStock = beliefFactory.createBeerStockBelief(model)
            return listOfNotNull(hasOwnerBeer, robotLocation, beerStock)
        } else {
            return emptyList()
        }
    }

    override fun updateData(newData: Map<String, Any>): HouseEnvironment =
        copy(data = newData).also {
            getPercepts().forEach { b -> logger?.info { b.purpose?.capitalize() } }
        }

    fun parseAction(actionName: String): HouseState? {
        val model = data["model"] as? HouseState ?: return null
        return when (actionName) {
            "open(fridge)" ->
                model.openFridge().also {
                    logger?.info { "The fridge is open" }
                }

            "close(fridge)" ->
                model.closeFridge().also {
                    logger?.info { "The fridge is closed" }
                }

            "pick(beer)" ->
                model.getBeer()?.also {
                    logger?.info { "The robot is carrying the beer" }
                }

            "hand_in(beer)" ->
                model.handInBeer()?.also {
                    logger?.info { "The robot handed the beer to the owner" }
                }

            "sip(beer)" ->
                model.sipBeer()?.also {
                    logger?.info { "The owner took a sip of the beer" }
                }

            else -> {
                val action = tangleStruct(actionName)

                if (actionName.startsWith("move_towards")) {
                    val destination = action?.args[0] as? Atom
                    destination?.let { d ->
                        model.moveTowards(d.value)?.also { updatedModel ->
                            logger?.info {
                                "Robot moved towards $destination," +
                                    " old position: ${model.robotPosition}," +
                                    " new position: ${updatedModel.robotPosition}"
                            }
                        }
                    } ?: model
                } else if (actionName.startsWith("deliver")) {
                    sleep(FAKE_DELIVERY_TIME_MS)
                    val numBeers = action?.args[1].toString().toIntOrNull()
                    numBeers?.let {
                        model.addBeer(it).also { updatedModel ->
                            logger?.info { "Delivered $it beers" }
                        }
                    }
                } else {
                    model.also { logger?.warn { "Unknown action: $actionName" } }
                }
            }
        }
    }

    override fun percept(): BeliefBase {
        perception = Perception.of(getPercepts())
        return super.percept()
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
        logger: MasLogger?,
    ): HouseEnvironment =
        HouseEnvironment(
            agentIDs,
            externalActions,
            messageBoxes,
            perception,
            data,
            logger,
        )

    companion object {
        const val FAKE_DELIVERY_TIME_MS = 200L

        internal const val DEFAULT_GRID_SIZE = 5

        internal val defaultObjects =
            mapOf(
                "fridge" to Cell(0, 0),
                "owner" to Cell(DEFAULT_GRID_SIZE - 1, DEFAULT_GRID_SIZE - 1),
            )

        internal val DEFAULT_START_POSITION = Position(DEFAULT_GRID_SIZE / 2, DEFAULT_GRID_SIZE / 2)

        internal val defaultHouseData = mapOf("model" to HouseState())
    }
}
