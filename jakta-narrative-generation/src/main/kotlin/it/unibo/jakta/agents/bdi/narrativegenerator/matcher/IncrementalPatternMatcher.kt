package it.unibo.jakta.agents.bdi.narrativegenerator.matcher

import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

/**
 * Incremental pattern matcher that maintains a pool of partial matches
 */
data class IncrementalPatternMatcher(
    val logger: NarrativeGenerationLogger? = null,
    val partialMatches: List<PartialMatch> = emptyList(),
    val completedMatches: List<PatternMatch> = emptyList(),
    val newCompletedMatches: List<PatternMatch> = emptyList(),
) {
    private fun log(message: String) = logger?.info { message }

    /**
     * Process a new event against all active patterns
     */
    fun processEvent(
        eventId: String,
        baseKB: KnowledgeBase,
        eventsKB: KnowledgeBase,
        patterns: List<SiftingPattern>,
    ): IncrementalPatternMatcher {
        log("Processing event \"$eventId\" against ${patterns.size} patterns")

        // Create new partial matches from patterns
        val newStartMatches =
            patterns.mapNotNull { pattern ->
                log("Trying to start match with pattern: ${pattern.name}")
                val newMatch = tryStartMatch(pattern, baseKB, eventId)
                if (newMatch != null) {
                    log("Created new partial match for pattern ${pattern.name}, clauseIndex=${newMatch.clauseIndex}")
                } else {
                    log("Failed to start match with pattern ${pattern.name}")
                }
                newMatch
            }

        // Process existing partial matches
        log("Updating ${partialMatches.size} existing partial matches")
        val processedMatches =
            partialMatches.flatMap { partialMatch ->
                if (partialMatch.isDead || partialMatch.isComplete) {
                    listOf(partialMatch)
                } else {
                    log(
                        "Processing partial match for pattern ${partialMatch.pattern.name}, clauseIndex=${partialMatch.clauseIndex}",
                    )
                    val pattern = patterns.find { it.name == partialMatch.pattern.name }

                    if (pattern != null) {
                        // Check unless constraints
                        if (violatesUnlessConstraints(pattern, eventsKB, partialMatch)) {
                            log("Match violates unless constraints, marking as dead")
                            listOf(partialMatch.markDead())
                        } else {
                            // Try to advance the match
                            log("Trying to advance match")
                            val advancedMatch = tryAdvanceMatch(pattern, baseKB, partialMatch, eventId)

                            if (advancedMatch != null) {
                                log("Advanced match to clauseIndex=${advancedMatch.clauseIndex}")
                                // Keep both the original and advanced matches
                                listOf(partialMatch, advancedMatch)
                            } else {
                                log("Failed to advance match")
                                // No advancement, keep original
                                listOf(partialMatch)
                            }
                        }
                    } else {
                        log("Pattern ${partialMatch.pattern.name} not found in current patterns list")
                        listOf(partialMatch)
                    }
                }
            }

        // Filter active partial matches
        val activeMatches = (newStartMatches + processedMatches).filter { !it.isDead && !it.isComplete }

        // Find newly completed matches
        val newCompletions =
            (newStartMatches + processedMatches)
                .filter { it.isComplete && !completedMatches.contains(createPatternMatch(it)) }
                .map { createPatternMatch(it) }

        // Update completed matches
        val allCompletedMatches = completedMatches + newCompletions

        return copy(
            partialMatches = activeMatches,
            completedMatches = allCompletedMatches,
            newCompletedMatches = newCompletions,
        )
    }

    /**
     * Try to start a new partial match with the first event clause of a pattern
     */
    private fun tryStartMatch(
        pattern: SiftingPattern,
        kb: KnowledgeBase,
        eventId: String,
    ): PartialMatch? {
        if (pattern.clauses.isEmpty()) {
            log("Pattern ${pattern.name} has no clauses")
            return null
        }

        val firstClause = pattern.clauses.first()
        if (firstClause.isUnlessClause) {
            log("First clause is an unless clause, can't start match")
            return null
        }

//        log("Attempting to match first clause:\n $firstClause")
        val bindings: Substitution? = matchEventClause(firstClause, kb)
        if (bindings == null || bindings.isFailed) {
//            log("First clause did not match, no partial match created")
            return null
        }

        log("First clause matched with bindings: $bindings")
        return PartialMatch(
            pattern = pattern,
            clauseIndex = 1,
            bindings = bindings,
            matchedEventIds = listOf(eventId),
            isComplete = pattern.clauses.size == 1,
        )
    }

    /**
     * Try to advance an existing partial match with a new event
     */
    private fun tryAdvanceMatch(
        pattern: SiftingPattern,
        kb: KnowledgeBase,
        partialMatch: PartialMatch,
        eventId: String,
    ): PartialMatch? {
        if (partialMatch.clauseIndex >= pattern.clauses.size) {
            log("Match already at final clause, can't advance further")
            return null
        }

        val nextClause = pattern.clauses[partialMatch.clauseIndex]
        if (nextClause.isUnlessClause) {
            // Skip unless clauses for advancement (they're checked separately)
            log("Next clause is an unless clause, skipping to next")
            return tryAdvanceMatch(
                pattern,
                kb,
                partialMatch.copy(clauseIndex = partialMatch.clauseIndex + 1),
                eventId,
            )
        }

        val newBindings = matchEventClause(nextClause, kb, partialMatch.bindings)
        if (newBindings == null || newBindings.isFailed) {
            return null
        } else {
            log("Clause matched with bindings: $newBindings")
        }

//        log("Clause matched with new bindings: $newBindings")
//        val remainingClauses = pattern.clauses.size - (partialMatch.clauseIndex + 1)
        val remainingNonUnlessClauses =
            pattern.clauses
                .drop(partialMatch.clauseIndex + 1)
                .count { !it.isUnlessClause }
        val isComplete = remainingNonUnlessClauses == 0

        if (isComplete) {
            log("Match will be complete after this advancement")
        } else {
            log("Match still needs $remainingNonUnlessClauses more non-unless clauses to be completed")
        }

        return partialMatch.copy(
            clauseIndex = partialMatch.clauseIndex + 1,
            bindings = partialMatch.bindings.plus(newBindings),
            matchedEventIds = partialMatch.matchedEventIds + eventId,
            isComplete = isComplete,
        )
    }

    /**
     * Check if an event violates any unless constraints for a partial match
     */
    private fun violatesUnlessConstraints(
        pattern: SiftingPattern,
        kb: KnowledgeBase,
        partialMatch: PartialMatch,
    ): Boolean {
        val unlessClauses = pattern.clauses.filter { it.isUnlessClause }
        if (unlessClauses.isEmpty()) {
            log("No unless constraints to check")
            return false
        }

        log("Checking ${unlessClauses.size} unless constraints")
        val violations =
            unlessClauses.filter { unlessClause ->
                val matches = matchEventClause(unlessClause, kb, partialMatch.bindings)
                if (matches != null && matches.isSuccess) {
                    log("Constraint violated")
                } else {
                    log("Constraint not violated")
                }
                matches?.isSuccess == true
            }

        return violations.isNotEmpty()
    }

    /**
     * Match an event against a pattern clause
     */
    private fun matchEventClause(
        clause: PatternClause,
        kb: KnowledgeBase,
        existingBindings: Substitution = Substitution.empty(),
    ): Substitution? {
        val query = buildEventQuery(clause, existingBindings)
        if (query == null) {
            log("Failed to build query for clause: $clause")
            return null
        }

        log("Executing query: $query")
        val result = kb.solve(query).substitution
//        log("Query matched with substitution: $result")
        return result
    }

    /**
     * Build a query for matching an event against a clause
     */
    private fun buildEventQuery(
        clause: PatternClause,
        existingBindings: Substitution,
    ): Struct? =
        clause.rule
            ?.head
            ?.apply(existingBindings)
            ?.castToStruct()

    /**
     * Convert a completed partial match to a PatternMatch
     */
    private fun createPatternMatch(partialMatch: PartialMatch): PatternMatch {
        log("Creating PatternMatch from completed PartialMatch for pattern: ${partialMatch.pattern.name}")
        log("Final bindings: ${partialMatch.bindings}")
        log("Matched events: ${partialMatch.matchedEventIds}")

        return PatternMatch(
            pattern = partialMatch.pattern,
            bindings = partialMatch.bindings,
        )
    }

    fun printMatchStatus() {
        log("=== MATCH STATUS ===")
        log("Active partial matches: ${partialMatches.size}")
        partialMatches.forEachIndexed { index, match ->
            log(
                "  [$index] Pattern: ${match.pattern.name}, clauseIndex: ${match.clauseIndex}, bindings: ${match.bindings}",
            )
        }
        log("Completed matches: ${completedMatches.size}")
        completedMatches.forEachIndexed { index, match ->
            log("  [$index] Pattern: ${match.pattern.name}")
        }
        log("===================")
    }
}
