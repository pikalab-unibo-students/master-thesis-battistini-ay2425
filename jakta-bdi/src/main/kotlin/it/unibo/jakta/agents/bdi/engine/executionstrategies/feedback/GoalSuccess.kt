package it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback

import it.unibo.jakta.agents.bdi.engine.actions.ActionSignature
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.termFormatter
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializableTerm
import it.unibo.tuprolog.core.Term
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("GoalSuccess")
sealed interface GoalSuccess : PositiveFeedback {
    @Serializable
    @SerialName("GoalExecutionSuccess")
    data class GoalExecutionSuccess(
        val goalExecuted: Goal,
        override val description: String?,
    ) : GoalSuccess {
        constructor(goalExecuted: Goal) : this(
            goalExecuted,
            "The goal $goalExecuted was successfully executed",
        )
    }

    @Serializable
    @SerialName("ActionSuccess")
    data class ActionSuccess(
        val actionSignature: ActionSignature,
        val providedArguments: List<SerializableTerm>,
        override val description: String?,
    ) : GoalSuccess {
        constructor(actionSignature: ActionSignature, providedArguments: List<Term>) : this(
            actionSignature,
            providedArguments,
            "The action \"${actionSignature.name}\" was successfully executed with the given arguments: " +
                providedArguments.joinToString(", ") { termFormatter.format(it) },
        )
    }
}
