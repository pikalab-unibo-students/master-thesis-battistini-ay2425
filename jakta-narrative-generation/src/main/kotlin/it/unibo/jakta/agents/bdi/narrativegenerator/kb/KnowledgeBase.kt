package it.unibo.jakta.agents.bdi.narrativegenerator.kb

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.impl.KnowledgeBaseImpl
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.solve.Solution
import java.util.UUID
import kotlin.collections.forEach

interface KnowledgeBase : Iterable<Clause> {
    val eventSerializer: EventSerializer
    val logger: NarrativeGenerationLogger?

    fun add(
        logEntry: LogEntry,
        id: String = UUID.randomUUID().toString(),
    ): KBRetrievalResult

    fun add(clause: Clause): KnowledgeBase

    fun addAll(clause: Iterable<Clause>): KnowledgeBase = clause.fold(this) { acc, rule -> acc.add(rule) }

    fun solve(struct: Struct): Solution

    fun solveAll(struct: Struct): Sequence<Solution>

    operator fun plus(other: KnowledgeBase): KnowledgeBase = other.fold(this) { acc, clause -> acc.add(clause) }

    companion object {
        /** @return an empty [KnowledgeBase] */
        fun empty(
            converter: EventSerializer,
            logger: NarrativeGenerationLogger? = null,
        ): KnowledgeBase = KnowledgeBaseImpl(converter, logger)

        /**
         * Generates a [KnowledgeBase] from a collection of [Rule]
         * @param rules: the [Iterable] of [Rule] the [KnowledgeBase] will be composed of
         * @return the new [KnowledgeBase]
         */
        fun of(
            converter: EventSerializer,
            logger: NarrativeGenerationLogger? = null,
            rules: Iterable<Rule>,
        ): KnowledgeBase {
            var bb = empty(converter)
            rules.forEach { bb = bb.add(it) }
            return bb
        }

        fun of(
            converter: EventSerializer,
            logger: NarrativeGenerationLogger? = null,
            vararg rules: Rule,
        ): KnowledgeBase = of(converter, logger, rules.asList())

        val termFormatter: TermFormatter = TermFormatter.prettyExpressions()
    }
}
