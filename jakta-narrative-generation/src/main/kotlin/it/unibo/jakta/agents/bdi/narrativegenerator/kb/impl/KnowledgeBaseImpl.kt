package it.unibo.jakta.agents.bdi.narrativegenerator.kb.impl

import it.unibo.jakta.agents.bdi.engine.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.ClauseUpdate
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KBRetrievalResult
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase.Companion.termFormatter
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.format
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class KnowledgeBaseImpl(
    private val clauses: ClauseMultiSet,
    override val eventSerializer: EventSerializer,
    override val logger: NarrativeGenerationLogger? = null,
) : KnowledgeBase {
    constructor(eventSerializer: EventSerializer, logger: NarrativeGenerationLogger?) : this(
        ClauseMultiSet.empty(Unificator.default),
        eventSerializer,
        logger,
    )

    override fun iterator(): Iterator<Clause> = clauses.filterIsInstance<Clause>().iterator()

    override fun add(
        logEntry: LogEntry,
        id: String,
    ): KBRetrievalResult {
        val logEventContainer = logEntry.message
        val clauses = eventSerializer.convert(id, logEventContainer.event).map { Clause.of(it) }

        val timestamp =
            try {
                val localDate = LocalDateTime.parse(logEntry.timestamp.substringBefore("Z"))
                val timeInMilliseconds = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
                timeInMilliseconds
            } catch (_: Exception) {
                logEntry.timestamp.toLong()
            }
        val timeClause = Clause.of(Struct.of("time", Atom.of(id), Numeric.of(timestamp)))

        val agentID =
            if (logEntry.message.event !is EnvironmentChange) {
                Clause.of(
                    Struct.of("agent", Atom.of(id), Atom.of(logEventContainer.agentID?.name.toString())),
                )
            } else {
                null
            }

        val updatedClauses =
            (clauses + timeClause + agentID).filterNotNull().also {
                logger?.info {
                    "Added ${it.size} clauses to the KB:\n${it.joinToString("\n") { c -> c.format(termFormatter) }}"
                }
            }
        val updatedKB = this.addAll(updatedClauses)
        return KBRetrievalResult(
            updatedClauses.map { ClauseUpdate.addition(it) },
            updatedKB,
        )
    }

    override fun add(clause: Clause): KnowledgeBase = KnowledgeBaseImpl(clauses.add(clause), eventSerializer, logger)

    override fun solve(struct: Struct): Solution = createSolver().solveOnce(struct)

    override fun solveAll(struct: Struct): Sequence<Solution> = createSolver().solve(struct)

    private fun createSolver(): Solver =
        Solver.prolog
            .newBuilder()
            .flag(Unknown, Unknown.FAIL)
            .staticKb(Theory.of(clauses))
            .flag(TrackVariables) { ON }
            .build()

    override fun toString(): String =
        clauses.joinToString("\n") { rule ->
            if (rule.body == Truth.TRUE) {
                "${termFormatter.format(rule.head!!)}."
            } else {
                buildString {
                    appendLine("${termFormatter.format(rule.head!!)} :-")
                    val bodyText =
                        rule.body
                            .accept(termFormatter)
                            .replace("), ", "), \n")
                            .split("\n")
                            .joinToString("\n") { "    $it" } + "."
                    append(bodyText)
                }
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnowledgeBaseImpl

        return clauses == other.clauses
    }

    override fun hashCode(): Int = clauses.hashCode()
}
