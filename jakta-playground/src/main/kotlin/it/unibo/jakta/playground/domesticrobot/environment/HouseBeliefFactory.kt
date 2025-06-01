package it.unibo.jakta.playground.domesticrobot.environment

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief.Companion.SOURCE_PERCEPT
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct

class HouseBeliefFactory {
    fun createHasOwnerBelief(model: HouseState): Belief? =
        if (model.sipCount > 0) {
            val struct = Struct.of("has", Atom.of("owner"), Atom.of("beer"))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "the owner has a beer",
            )
        } else {
            null
        }

    fun createRobotLocationBelief(model: HouseState): Belief? {
        val objectReached =
            model.objects
                .filter { it.value.asPosition().isOn(model.robotPosition) }
                .map { it.key }
                .firstOrNull()
        return objectReached?.let {
            val struct = Struct.of("at", Atom.of("robot"), Atom.of(objectReached))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "the robot is at $objectReached",
            )
        }
    }

    fun createBeerStockBelief(model: HouseState): Belief? =
        if (model.fridgeOpen) {
            val struct = Struct.of("stock", Atom.of("beer"), Numeric.of(model.availableBeers))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "the fridge has ${model.availableBeers} beers",
            )
        } else {
            null
        }
}
