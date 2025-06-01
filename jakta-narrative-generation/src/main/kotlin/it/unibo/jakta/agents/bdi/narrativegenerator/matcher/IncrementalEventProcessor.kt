package it.unibo.jakta.agents.bdi.narrativegenerator.matcher

import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern

data class IncrementalEventProcessor(
    val baseKB: KnowledgeBase,
    val eventKB: KnowledgeBase,
    val patterns: List<SiftingPattern>,
    val logger: NarrativeGenerationLogger? = null,
    val matcher: IncrementalPatternMatcher = IncrementalPatternMatcher(logger),
    val eventMatchResults: List<EventProcessingResult> = emptyList(),
    private val eventCounter: Long = 0L,
) {
    fun matchAll(logEntries: List<LogEntry>): IncrementalEventProcessor =
        logEntries.fold(this) { acc, logEntry -> acc.match(logEntry) }

    fun match(logEntry: LogEntry): IncrementalEventProcessor {
        val updatedEventCounter = eventCounter + 1
        val eventId = "event_$updatedEventCounter"

        val (modifiedClauses, updatedEventKB) = eventKB.add(logEntry, eventId)
        val clauses =
            modifiedClauses
                .filter { it.updateType == ContextUpdate.ADDITION }
                .map { it.clause }
        val baseKB = baseKB.addAll(clauses)

        val newMatcher =
            matcher.processEvent(
                eventId,
                baseKB,
                (baseKB + updatedEventKB),
                patterns,
            )
        newMatcher.printMatchStatus()

        val res =
            EventProcessingResult(
                eventId = eventId,
                newMatches = newMatcher.newCompletedMatches,
                activePartialMatches = newMatcher.partialMatches.size,
            )
        return copy(
            eventCounter = updatedEventCounter,
            eventKB = updatedEventKB,
            matcher = newMatcher,
            eventMatchResults = eventMatchResults + res,
        )
    }
}
