package it.unibo.jakta.playground.evaluation.scripts

import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.EventType
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.StopWatch
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer.Companion.structOfEvent
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.PatternClauseMetadata.meaning
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.pattern
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import it.unibo.jakta.agents.bdi.narrativegenerator.matcher.IncrementalEventProcessor
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.message.ObjectMessage
import org.koin.ksp.generated.module

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

fun main() {
    JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)

//    val msg = SendMessage(
//        Message("sender", Achieve, Struct.of("ordered", Atom.of("beer"), Integer.of(10))),
//        "recipient"
//    )

    val logger =
        NarrativeGenerationLogger.create(
            "PatternMatcher",
            LoggingConfig(
                logToServer = false,
                logToFile = true,
            ),
        )

    val violationOfHospitality =
        pattern("violationOfHospitality") {
            val e1 = varOf("E1")
            val e2 = varOf("E2")
            val e3 = varOf("E3")
            val eMid = varOf("EMid")
            val guest = varOf("Guest")
            val host = varOf("Host")
            val harmType = varOf("HarmType")

            +event(e1) {
                +"type"(e1, "enterTown")
                +"agent"(e1, guest)
            }.meaning {
                +"$guest entered the town"
            }

            +event(e2) {
                +"type"(e2, "showHospitality")
                +"agent"(e2, host)
                +"target"(e2, guest)
                +"agent_value"(guest, "communalism")
            }.meaning {
                +"$host, who values communalism, showed hospitality to $guest"
            }

            +event(e3) {
                +"tag"(e3, "harm")
                +"type"(e3, harmType)
                +"agent"(e3, host)
                +"target"(e3, guest)
            }.meaning {
                +"However, $host harmed $guest ($harmType), violating the norms of hospitality"
            }

            -event(eMid) {
                +"type"(eMid, "leaveTown")
                +"agent"(eMid, guest)
                between(e1, e3)
            }.meaning {
                +"$guest did not leave town in the meantime"
            }
        }

    val patterns = listOf(violationOfHospitality)
    val config =
        EventSerializerConfig().let {
            it
                .withTermMapping(EventType::class) { eventType, id, path ->
                    listOf(structOfEvent(id, "type", Atom.of(eventType.type)))
                }.withTermMapping(PatternClause::class) { eventType, id, path ->
                    eventType.constraints.map { constraint ->
                        val functor = constraint.functor
                        val args = constraint.args
                        structOfEvent(id, "${args[0].asVar()?.name?.lowercase()}_$functor", args[1])
                    }
                }
        }
    val converter = EventSerializer.of(config)

    val emin = AgentID("Emin")
    val mira = AgentID("Mira")

    val events: List<LogEntry> =
        listOf(
            LogEntry.create(GenericEvent.of("enterTown", emin), agentID = emin),
            LogEntry.create(GenericEvent.of("showHospitality", mira, emin), agentID = mira),
            LogEntry.create(GenericEvent.of("steal", mira, emin, setOf("harm")), agentID = mira),
            LogEntry.create(GenericEvent.of("attack", mira, emin, setOf("harm")), agentID = mira),
        )

    val kb =
        KnowledgeBase
            .empty(converter, logger)
            .add(Clause.of(Struct.of("agent_value", Atom.of(emin.name), Atom.of("communalism"))))
            .let { kb ->
                patterns.fold(kb) { currentKb, pattern ->
                    currentKb.addAll(pattern.clauses.mapNotNull { it.rule })
                }
            }

    val stopWatch = StopWatch()
    val eventProcessor = IncrementalEventProcessor(kb, patterns, stopWatch, logger)
    val finalProcessor = eventProcessor.matchAll(events)

    finalProcessor.eventMatchResults.last().activePartialMatches shouldBe 2

    val patternMatches = finalProcessor.matcher.completedMatches
    patternMatches.size shouldBe 2

    val msg = PatternMatchLogEvent.from(patternMatches.first(), events.last().timestamp)

    logger.info { ObjectMessage(LogEventContext(msg)) }

    val clauses = kb.add(LogEntry.create(msg), "id").modifiedClauses.map { it.clause }
    println(clauses)
}
