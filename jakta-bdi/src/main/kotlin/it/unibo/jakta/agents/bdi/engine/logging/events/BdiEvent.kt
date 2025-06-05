package it.unibo.jakta.agents.bdi.engine.logging.events

import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.triggerFormatter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BDIEvent")
sealed interface BdiEvent : AgentEvent {
    @Serializable
    @SerialName("EventSelected")
    data class EventSelected(
        val event: Event,
        override val description: String?,
    ) : BdiEvent {
        constructor(event: Event) : this(
            event,
            "Selected ${eventType(event)} event: ${triggerFormatter.format(event.trigger)}",
        )
    }

    companion object {
        fun eventType(e: Event) =
            if (e.isExternal()) {
                "external"
            } else {
                "internal"
            }
    }
}
