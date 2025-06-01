package it.unibo.jakta.playground.explorer

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.goals.TriggerMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.plans
import it.unibo.jakta.agents.bdi.engine.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.engine.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief.Companion.SOURCE_PERCEPT
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import it.unibo.jakta.playground.gridworld.GridWorld
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

object ExplorerBot {
    private val Direction = Var.of("Direction")
    private val Object = Var.of("Object")

    fun baselinePlans() =
        plans {
            +achieve("reach"(Object)) onlyIf {
                "there_is"(Object, "here").fromPercept
            }
            +achieve("reach"(Object)) onlyIf {
                "there_is"(Object, Direction).fromPercept
            } then {
                execute("move"(Direction))
            }
            +achieve("reach"(Object)) onlyIf {
                not("there_is"(Object, `_`).fromPercept)
            } then {
                execute("getDirectionToMove"(Direction))
                execute("move"(Direction))
                achieve("reach"(Object))
            }
        }

    fun MasScope.explorerBot(
        plans: Iterable<Plan>? = null,
        strategy: LMGenerationStrategy? = null,
    ) = agent("ExplorerBot") {
        generationStrategy = strategy
        goals {
            admissible {
                +achieve("reach"("Object")).meaning {
                    "reach a situation where ${args[0]} is in the position of the agent" +
                        " (i.e. there_is(${args[0]}, here))"
                }
            }
            +achieve("reach"("home"))
        }
        beliefs {
            admissible {
                +fact { "obstacle"("Direction") }.meaning {
                    "there is an $functor to the ${args[0]}"
                }
                +fact { "there_is"("Object", "Direction") }.meaning {
                    "there is an ${args[0]} in the given ${args[1]}"
                }
                +fact { "direction"("Direction") }.meaning {
                    "${args[0]} is a direction"
                }
                +fact { "object"("Object") }.meaning {
                    "${args[0]} is an object"
                }
            }
        }
        plans?.let { plans(it) }
    }

    val getDirectionToMove =
        object : AbstractExternalAction("getDirectionToMove", "Direction") {
            override fun action(request: ExternalRequest) {
                val percepts = request.environment.perception.percept()
                val output = request.arguments[0].asVar()

                val belief =
                    Belief.wrap(
                        Struct.of("free", Var.of("Direction")),
                        wrappingTag = SOURCE_PERCEPT,
                    )

                val dir =
                    percepts
                        .solveAll(belief, ignoreSource = true)
                        .toList()
                        .flatMap { it.substitution.values }
                        .random()

                if (output != null) {
                    this.addResults(Substitution.unifier(output to dir))
                }
            }
        }

    val move =
        object : AbstractExternalAction("move", "Direction") {
            override fun action(request: ExternalRequest) {
                val direction = request.arguments[0].asAtom()?.value
                if (direction != null) {
                    updateData("directionToMove" to direction)
                }
            }
        }

    fun MasScope.gridWorld() =
        environment {
            from(GridWorld())
            actions {
                action(move).meaning {
                    "move in the given ${args[0]}"
                }
                action(getDirectionToMove).meaning {
                    "provides a ${args[0]} free of obstacles where the agent can then move"
                }
            }
        }
}
