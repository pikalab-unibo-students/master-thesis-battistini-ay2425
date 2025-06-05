package it.unibo.jakta.agents.bdi.engine.logging.events

import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.triggerFormatter
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PlanEvent")
sealed interface PlanEvent : AgentEvent {
    @Serializable
    @SerialName("PlanSelected")
    data class PlanSelected(
        val plan: Plan,
        override val description: String?,
    ) : PlanEvent {
        constructor(plan: Plan) : this(plan, "Selected plan: ${triggerFormatter.format(plan.trigger)}")
    }
}
