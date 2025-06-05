package it.unibo.jakta.agents.bdi.narrativegenerator.logging

import it.unibo.jakta.agents.bdi.engine.logging.events.EventType
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.narrativegenerator.matcher.PatternMatch
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PatternMatch")
data class PatternMatchLogEvent(
    override val eventType: EventType,
    override val description: String?,
    val clauses: List<PatternClause>,
    val originalTimestamp: String,
) : LogEvent {
    companion object {
        fun from(
            match: PatternMatch,
            originalTimestamp: String,
        ): PatternMatchLogEvent {
            val compiledDescription = match.getCompiledDescription()
            return PatternMatchLogEvent(
                EventType(match.pattern.name),
                compiledDescription,
                match.pattern.clauses,
                originalTimestamp,
            )
        }
    }
}
