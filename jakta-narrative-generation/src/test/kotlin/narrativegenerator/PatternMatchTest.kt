package narrativegenerator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.EventType
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer.Companion.structOfEvent
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.PatternClauseMetadata.meaning
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.pattern
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.matcher.IncrementalEventProcessor
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct

class PatternMatchTest :
    FunSpec({
        context("Verify violationOfHospitality") {
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
                EventSerializerConfig().withTermMapping(EventType::class) { eventType, id, path ->
                    listOf(structOfEvent(id, "type", Atom.of(eventType.type)))
                }
            val converter = EventSerializer.of(config)

            val emin = AgentID("Emin")
            val mira = AgentID("Mira")

            val logger = NarrativeGenerationLogger.of("PatternMatcher", LoggingConfig())

            test("check that matches the correct events") {
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

                val eventProcessor =
                    IncrementalEventProcessor(kb, KnowledgeBase.empty(converter, logger), patterns, logger)
                val finalProcessor = eventProcessor.matchAll(events)

                finalProcessor.eventMatchResults.last().activePartialMatches shouldBe 2

                val patternMatches = finalProcessor.matcher.completedMatches
                patternMatches.size shouldBe 2

                patternMatches.first().getCompiledDescription() shouldBe
                    """
                    'Emin' entered the town
                    'Mira', who values communalism, showed hospitality to 'Emin'
                    However, 'Mira' harmed 'Emin' (steal), violating the norms of hospitality
                    'Emin' did not leave town in the meantime
                    """.trimIndent()
            }

            test("check that does not match the correct events") {
                val events: List<LogEntry> =
                    listOf(
                        LogEntry.create(GenericEvent.of("enterTown", emin), agentID = emin, timestamp = "1"),
                        LogEntry.create(
                            GenericEvent.of("showHospitality", mira, emin),
                            agentID = mira,
                            timestamp = "2",
                        ),
                        LogEntry.create(GenericEvent.of("leaveTown", emin), agentID = emin, timestamp = "3"),
                        LogEntry.create(
                            GenericEvent.of("steal", mira, emin, setOf("harm")),
                            agentID = mira,
                            timestamp = "4",
                        ),
                        LogEntry.create(
                            GenericEvent.of("attack", mira, emin, setOf("harm")),
                            agentID = mira,
                            timestamp = "5",
                        ),
                    )

                val kb =
                    KnowledgeBase
                        .empty(converter)
                        .add(Clause.of(Struct.of("agent_value", Atom.of(emin.name), Atom.of("communalism"))))
                        .let { kb ->
                            patterns.fold(kb) { currentKb, pattern ->
                                currentKb.addAll(pattern.clauses.mapNotNull { it.rule })
                            }
                        }.also {
                            println(it)
                        }

                val eventProcessor =
                    IncrementalEventProcessor(kb, KnowledgeBase.empty(converter, logger), patterns, logger)
                val finalProcessor = eventProcessor.matchAll(events)

                finalProcessor.eventMatchResults.last().activePartialMatches shouldBe 0
                finalProcessor.eventMatchResults.last().newMatches shouldBe emptyList()

                val patternMatches = finalProcessor.matcher.completedMatches
                patternMatches.size shouldBe 0
            }
        }
    })
