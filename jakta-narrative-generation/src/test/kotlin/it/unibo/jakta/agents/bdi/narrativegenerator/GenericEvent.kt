package it.unibo.jakta.agents.bdi.narrativegenerator

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.logging.events.EventType
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import kotlinx.serialization.Serializable

@Serializable
data class GenericEvent(
    override val eventType: EventType,
    val actor: AgentID,
    val target: AgentID? = null,
    val tag: Set<String> = emptySet(),
    override val description: String?,
) : LogEvent {
    companion object {
        fun of(
            type: String,
            actor: AgentID,
            target: AgentID? = null,
            tag: Set<String> = emptySet(),
        ) = GenericEvent(
            EventType(type),
            actor,
            target,
            tag,
            "$type from ${actor.name} to ${target?.name ?: "nothing"}",
        )
    }
}
