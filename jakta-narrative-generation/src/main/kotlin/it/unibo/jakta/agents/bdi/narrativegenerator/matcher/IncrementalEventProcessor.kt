package it.unibo.jakta.agents.bdi.narrativegenerator.matcher

import it.unibo.jakta.agents.bdi.narrativegenerator.StopWatch
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern

data class IncrementalEventProcessor(
    val kb: KnowledgeBase,
    val patterns: List<SiftingPattern>,
    private val stopWatch: StopWatch? = null,
    private val logger: NarrativeGenerationLogger? = null,
    val matcher: IncrementalPatternMatcher = IncrementalPatternMatcher(logger),
    val eventMatchResults: List<EventProcessingResult> = emptyList(),
    private val eventCounter: Long = 0L,
) {
    fun matchAll(logEntries: List<LogEntry>): IncrementalEventProcessor =
        logEntries.fold(this) { acc, logEntry -> acc.match(logEntry) }

    fun match(logEntry: LogEntry): IncrementalEventProcessor {
        stopWatch?.reset()
        stopWatch?.start()
        val updatedEventCounter = eventCounter + 1
        val eventId = "event_$updatedEventCounter"

        val (_, updatedEventKB) = kb.add(logEntry, eventId)

        val newMatcher =
            matcher.processEvent(
                eventId,
                updatedEventKB,
                patterns,
            )
        newMatcher.printMatchStatus()
        stopWatch?.stop()

        val elapsedTime = stopWatch?.getElapsedTime()
        val res =
            EventProcessingResult(
                eventId = eventId,
                newMatches = newMatcher.newCompletedMatches,
                activePartialMatches = newMatcher.partialMatches.size,
                timeTook = elapsedTime,
            )
        return copy(
            eventCounter = updatedEventCounter,
            matcher = newMatcher,
            eventMatchResults = eventMatchResults + res,
        )
    }
}
