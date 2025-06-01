package it.unibo.jakta.agents.bdi.narrativegenerator.matcher

import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Substitution

/**
 * Result of processing a single event
 */
data class EventProcessingResult(
    val eventId: String,
    val newMatches: List<PatternMatch>,
    val activePartialMatches: Int,
)

/**
 * Represents a partial match in the incremental execution pool
 */
data class PartialMatch(
    val pattern: SiftingPattern,
    val clauseIndex: Int,
    val bindings: Substitution,
    val matchedEventIds: List<String> = emptyList(),
    val isDead: Boolean = false,
    val isComplete: Boolean = false,
) {
    fun markDead(): PartialMatch = copy(isDead = true)
}
