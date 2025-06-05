package it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback

import it.unibo.jakta.agents.bdi.engine.actions.ActionSignature
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.termFormatter
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializableTerm
import it.unibo.tuprolog.core.Struct
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("GoalFailure")
sealed interface GoalFailure : NegativeFeedback {
    @Serializable
    @SerialName("InvalidActionArityError")
    data class InvalidActionArityError(
        val actionSignature: ActionSignature,
        val providedArguments: List<SerializableTerm>,
        override val description: String?,
    ) : GoalFailure {
        constructor(actionSignature: ActionSignature, providedArguments: List<SerializableTerm>) : this(
            actionSignature,
            providedArguments,
            "The arity of the action \"${actionSignature.name}\" is not correct: expected ${actionSignature.arity}, " +
                "found ${providedArguments.map { termFormatter.format(it)}}",
        )
    }

    @Serializable
    @SerialName("ActionSubstitutionFailure")
    data class ActionSubstitutionFailure(
        val actionSignature: ActionSignature,
        val providedArguments: List<SerializableTerm>,
        override val description: String?,
    ) : GoalFailure {
        constructor(actionSignature: ActionSignature, providedArguments: List<SerializableTerm>) : this(
            actionSignature,
            providedArguments,
            "The action \"${actionSignature.name}\" could not be applied with the given arguments: " +
                "expected ${actionSignature.arity}, found ${providedArguments.map { termFormatter.format(it) }}",
        )
    }

    @Serializable
    @SerialName("ActionNotFound")
    data class ActionNotFound(
        val availableActions: List<ActionSignature>,
        val actionNotFoundName: String,
        override val description: String?,
    ) : GoalFailure {
        constructor(availableActions: List<ActionSignature>, actionNotFoundName: String) : this(
            availableActions,
            actionNotFoundName,
            "The action \"${actionNotFoundName}\" could not be found among the available actions: " +
                availableActions.joinToString(", ") { it.name },
        )
    }

    @Serializable
    @SerialName("ActionFailure")
    data class ActionFailure(
        val actionSignature: ActionSignature,
        val providedArguments: List<SerializableTerm>,
        override val description: String?,
    ) : GoalFailure {
        constructor(actionSignature: ActionSignature, providedArguments: List<SerializableTerm>) : this(
            actionSignature,
            providedArguments,
            "The action \"${actionSignature.name}\" failed with the given arguments: " +
                "${providedArguments.map { termFormatter.format(it) }}",
        )
    }

    @Serializable
    @SerialName("TestGoalFailureFeedback")
    data class TestGoalFailureFeedback(
        val goalTested: Struct,
        override val description: String?,
    ) : GoalFailure {
        constructor(goalTested: Struct) : this(goalTested, "The goal $goalTested could not be tested")
    }
}
