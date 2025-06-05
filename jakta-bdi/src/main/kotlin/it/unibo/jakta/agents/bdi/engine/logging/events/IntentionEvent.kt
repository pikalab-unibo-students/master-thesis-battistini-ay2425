package it.unibo.jakta.agents.bdi.engine.logging.events

import it.unibo.jakta.agents.bdi.engine.intentions.Intention
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IntentionEvent")
sealed interface IntentionEvent : AgentEvent {
    val intention: Intention

    @Serializable
    @SerialName("IntentionCreation")
    data class AssignPlanToNewIntention(
        override val intention: Intention,
        override val description: String?,
    ) : IntentionEvent {
        constructor(intention: Intention) : this(intention, "Created new intention ${intention.id.id}")
    }

    @Serializable
    @SerialName("IntentionUpdate")
    data class AssignPlanToExistingIntention(
        override val intention: Intention,
        override val description: String?,
    ) : IntentionEvent {
        constructor(intention: Intention) : this(intention, "Updated intention ${intention.id.id}")
    }

    @Serializable
    @SerialName("IntentionGoalRun")
    data class IntentionGoalRun(
        override val intention: Intention,
        override val description: String?,
    ) : IntentionEvent {
        constructor(intention: Intention) : this(intention, "Running goal of ${intention.id.id}")
    }
}
