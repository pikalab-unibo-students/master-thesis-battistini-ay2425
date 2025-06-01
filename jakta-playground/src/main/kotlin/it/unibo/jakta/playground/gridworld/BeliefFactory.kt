package it.unibo.jakta.playground.gridworld

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief.Companion.SOURCE_PERCEPT
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class BeliefFactory {
    fun createDirectionBeliefs(state: GridWorldState) =
        state.availableDirections.map {
            val functor = "direction"
            val struct = Struct.of(functor, Atom.of(it.id))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "${it.id} is a $functor",
            )
        } +
            Belief.wrap(
                Struct.of("direction", Atom.of("here")),
                wrappingTag = SOURCE_PERCEPT,
                purpose = "here denotes the null direction w.r.t. the agent's current location",
            )

    fun createObjectBeliefs(state: GridWorldState) =
        state.objectsPosition.map {
            val functor = "object"
            val struct = Struct.of(functor, Atom.of(it.key))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "${it.key} is an $functor",
            )
        }

    fun createObstacleBeliefs(
        grid: Grid,
        state: GridWorldState,
    ) = state.availableDirections.filter { it != Direction.HERE }.map { direction ->
        val isObstacle = grid.isObstacleInDirection(state.agentPosition, direction)
        if (isObstacle) {
            val struct = Struct.of("obstacle", Atom.of(direction.id))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "there is an obstacle to the ${direction.id}",
            )
        } else {
            val struct = Struct.of("free", Atom.of(direction.id))
            Belief.wrap(
                struct,
                wrappingTag = SOURCE_PERCEPT,
                purpose = "there is no obstacle to the ${direction.id}",
            )
        }
    }

    fun createThereIsBeliefs(state: GridWorldState) =
        state.objectsPosition.mapNotNull { (objectName, position) ->
            val direction = state.agentPosition.directionTo(position)
            if (direction != null &&
                (
                    state.agentPosition.isAdjacentTo(position) ||
                        state.agentPosition.isOn(position)
                )
            ) {
                val struct = Struct.of("there_is", Atom.of(objectName), Atom.of(direction.id))
                Belief.wrap(
                    struct,
                    wrappingTag = SOURCE_PERCEPT,
                    purpose = "there is ${getIndefiniteArticle(objectName)} $objectName to the ${direction.id}",
                )
            } else {
                null
            }
        }

    companion object {
        /**
         * Determines the appropriate indefinite article ("a" or "an") for a given word
         * based on pronunciation rules rather than just spelling.
         *
         * @param word The word for which to determine the appropriate indefinite article
         * @return "a" or "an" based on the word's pronunciation
         */
        private fun getIndefiniteArticle(word: String): String {
            if (word.isBlank()) return "a"

            val w = word.trim().lowercase()
            if (w.isEmpty()) return "a"

            // Words that start with a vowel letter but have a consonant sound
            val usesAPrefix =
                listOf(
                    "eu",
                    "ewe",
                    "u",
                    "uni",
                    "use",
                    "user",
                    "util",
                    "usu",
                    "ubiq",
                    "uk",
                    "one",
                    "once",
                    "ou",
                    "y",
                )

            // Words that start with a consonant letter but have a vowel sound
            val usesAnPrefix =
                listOf(
                    "hour",
                    "heir",
                    "hon",
                    "honest",
                    "herb",
                    "x",
                )

            return when {
                // Special case for abbreviations and acronyms
                w.length == 1 -> {
                    // Single letters follow pronunciation rules
                    val vowelSoundLetters = listOf('a', 'e', 'f', 'h', 'i', 'l', 'm', 'n', 'o', 'r', 's', 'x')
                    if (w[0] in vowelSoundLetters) "an" else "a"
                }
                // Check for exceptions where words start with vowel but use "a"
                usesAPrefix.any { w.startsWith(it) } -> "a"
                // Check for exceptions where words start with consonant but use "an"
                usesAnPrefix.any { w.startsWith(it) } -> "an"
                // Standard vowel check
                w[0] in listOf('a', 'e', 'i', 'o', 'u') -> "an"
                // Default to "a" for consonant sounds
                else -> "a"
            }
        }
    }
}
