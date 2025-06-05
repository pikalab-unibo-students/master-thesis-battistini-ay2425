package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.jakta.agents.bdi.dsl.ScopeBuilder
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.goals.ActExternally
import it.unibo.jakta.agents.bdi.engine.goals.ActInternally
import it.unibo.jakta.agents.bdi.engine.goals.AddBelief
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.engine.goals.Spawn
import it.unibo.jakta.agents.bdi.engine.goals.Test
import it.unibo.jakta.agents.bdi.engine.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.engine.plangeneration.GenerationConfig
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.jakta.JaktaLogicProgrammingScope
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.reflect.KFunction

/**
 * Builder for Jakta Agents Plan body.
 * @param scope the [JaktaLogicProgrammingScope] it inherits from.
 */
class BodyScope(
    private val lpScope: Scope,
    private val generationConfig: GenerationConfig? = null,
) : ScopeBuilder<List<Goal>>,
    JaktaLogicProgrammingScope by JaktaLogicProgrammingScope.of(lpScope) {
    /**
     * The list of goals that the agent is going to execute during the plan execution.
     */
    private val goals = mutableListOf<Goal>()

    /**
     * Handler for the creation of a [Test] Goal.
     * @param goal the [Struct] that describes the agent's [Goal] trigger.
     */
    fun test(goal: Struct) {
        goals += Test.of(Belief.from(goal))
    }

    /**
     * Handler for the creation of a [Test] Goal.
     * @param goal the [String] representing the [it.unibo.tuprolog.core.Atom] that describes the agent's [Goal] trigger.
     */
    fun test(goal: String) = test(atomOf(goal))

    /**
     * Handler for the creation of an [Achieve] Goal on another intention.
     * This enables internal lifecycle concurrency.
     * @param goal the [Struct] that describes the Goal to [Achieve].
     */
    fun spawn(goal: Goal) {
        goals += Spawn.of(goal)
    }

    /**
     * Handler for the creation of a [GeneratePlan] Goal, optionally deciding to force the allocation on a new intention.
     * The allocation of a goal in a fresh intention enables internal lifecycle concurrency.
     * @param goal the [Struct] that describes the Goal to [GeneratePlan].
     * @param parallel a [Boolean] that indicates whether force the allocation on a fresh intention or not.
     */
    fun generate(
        goal: Goal,
        parallel: Boolean = false,
    ) {
        val genGoal = GeneratePlan.of(goal, generationConfig)
        goals += if (parallel) Spawn.of(genGoal) else genGoal
    }

    /**
     * Handler for the creation of an [Achieve] Goal, optionally deciding to force the allocation on a new intention.
     * The allocation of a goal in a fresh intention enables internal lifecycle concurrency.
     * @param goal the [Struct] that describes the Goal to [Achieve].
     * @param parallel a [Boolean] that indicates whether force the allocation on a fresh intention or not.
     */
    fun achieve(
        goal: Struct,
        parallel: Boolean = false,
    ) {
        goals += if (parallel) Spawn.of(Achieve.of(goal)) else Achieve.of(goal)
    }

    /**
     * Handler for the creation of an [Achieve] Goal, optionally deciding to force the allocation on a new intention.
     * The allocation of a goal in a fresh intention enables internal lifecycle concurrency.
     * @param goal the [String] representing the [it.unibo.tuprolog.core.Atom] that describes the Goal to [Achieve].
     * @param parallel a [Boolean] that indicates whether force the allocation on a fresh intention or not.
     */
    fun achieve(
        goal: String,
        parallel: Boolean = false,
    ) = achieve(atomOf(goal), parallel)

    /**
     * Handler for the addition of a [Belief] in the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase] annotated with self source.
     */
    operator fun Struct.unaryPlus() = add(this)

    operator fun String.unaryPlus() = add(atomOf(this))

    /**
     * Handler for the creation of a [Belief] in the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase] annotated with self source.
     * @param belief the [Struct] from which the [Belief] is created.
     */
    fun add(belief: Struct) {
        goals += AddBelief.of(Belief.wrap(belief, wrappingTag = Belief.SOURCE_SELF))
    }

    /**
     * Handler for the creation of a [Belief] in the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase] annotated with self source.
     * @param belief the [String] from which the [Belief] is created.
     */
    fun add(belief: String) = add(atomOf(belief))

    /**
     * Handler for the removal of a [Belief] from the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase].
     * The annotation of the [Belief] needs to be explicit.
     */
    operator fun Struct.unaryMinus() = remove(this)

    /**
     * Handler for the removal of a [Belief] from the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase].
     * The annotation of the [Belief] needs to be explicit.
     */
    fun remove(belief: Struct) {
        goals += RemoveBelief.of(Belief.from(belief))
    }

    fun remove(belief: String) = remove(atomOf(belief))

    /**
     * Handler for the update of a [Belief] value in the [it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase].
     * The annotation of the [Belief] needs to be explicit.
     */
    fun update(belief: Struct) {
        goals += UpdateBelief.of(Belief.from(belief))
    }

    fun update(belief: String) = update(atomOf(belief))

    /**
     * Handler for the creation of [Act] goal, which firstly look for action definition
     * into the [it.unibo.jakta.agents.bdi.engine.actions.InternalActions] and the in the [ExternalAction]s, declared in the environment.
     * @param struct the [Struct] that invokes the action.
     * @param externalOnly forces to search for action body only into [ExternalAction]s.
     */
    fun execute(
        struct: Struct,
        externalOnly: Boolean = false,
    ) {
        goals += if (externalOnly) ActExternally.of(struct) else Act.of(struct)
    }

    /**
     * Handler for the creation of [Act] goal, which firstly look for action definition
     * into the [it.unibo.jakta.agents.bdi.engine.actions.InternalActions] and the in the [ExternalAction]s, declared in the environment.
     * It firstly watches into the [it.unibo.jakta.agents.bdi.engine.actions.InternalActions] and the in the [ExternalAction]s contained into the environment.
     * @param input the [String] representing the [it.unibo.tuprolog.core.Atom] that invokes the action.
     * @param externalOnly forces to search for action body only into [ExternalAction]s.
     */
    fun execute(
        input: String,
        externalOnly: Boolean = false,
    ) = execute(atomOf(input), externalOnly)

    data class NamedWrapperForLambdas(
        val backingLambda: () -> Unit,
    ) : () -> Unit by backingLambda

    fun execute(
        externalAction: ExternalAction,
        vararg args: Any,
    ): Unit =
        when {
            externalAction.signature.arity == 0 -> {
                check(args.isEmpty()) { "External action ${externalAction.signature.name} does not accept parameters" }
                execute(externalAction.signature.name)
            }
            else -> {
                val argRefs =
                    args.map {
                        @Suppress("UNCHECKED_CAST")
                        when {
                            it::class.qualifiedName != null -> ObjectRef.of(it)
                            it is Function<*> -> NamedWrapperForLambdas(it as () -> Unit)
                            else -> error("Unsupported argument type: ${it::class.simpleName}")
                        }
                    }
                execute(externalAction.signature.name.invoke(ObjectRef.of(argRefs[0]), *argRefs.drop(1).toTypedArray()))
            }
        }

    fun execute(
        method: KFunction<*>,
        vararg args: Any,
    ): Unit =
        when {
            method.parameters.isEmpty() -> execute(method.name)
            else -> execute(method.name.invoke(args[0], *args.drop(1).toTypedArray()))
        }

    /**
     * Handler for the creation of a [ActInternally] Goal.
     * @param struct the [Struct] that invokes the action.
     */
    fun iact(struct: Struct) {
        goals += ActInternally.of(struct)
    }

    /**
     * Handler for the creation of a [ActInternally] Goal.
     * @param struct the [String] representing the [it.unibo.tuprolog.core.Atom] that invokes the action.
     */
    fun iact(struct: String) = iact(atomOf(struct))

    /**
     * Handler for the addition of a goals' list.
     * @param goalList the [List] of [Goal]s the agent is going to perform.
     */
    fun from(goalList: List<Goal>) =
        goalList.forEach {
            goals += it
        }

    override fun build(): List<Goal> = goals.toList()
}
