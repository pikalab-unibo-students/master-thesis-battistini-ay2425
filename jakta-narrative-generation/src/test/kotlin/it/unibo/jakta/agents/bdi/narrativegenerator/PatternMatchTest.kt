package it.unibo.jakta.agents.bdi.narrativegenerator

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

/**
 * From the tests provided by Winnow.
 *
 * @see [winnow-tests](https://github.com/mkremins/winnow/blob/master/tests.js)
 */
@Suppress("LocalVariableName", "ktlint:standard:property-naming")
class PatternMatchTest :
    FunSpec({
        context("Verify violationOfHospitality") {
            val violationOfHospitality =
                pattern("violationOfHospitality") {
                    val E1 = varOf("E1")
                    val E2 = varOf("E2")
                    val E3 = varOf("E3")
                    val eMid = varOf("EMid")
                    val Guest = varOf("Guest")
                    val Host = varOf("Host")
                    val HarmType = varOf("HarmType")

                    +event(E1) {
                        +"type"(E1, "enterTown")
                        +"agent"(E1, Guest)
                    }.meaning {
                        +"$Guest entered the town"
                    }

                    +event(E2) {
                        +"type"(E2, "showHospitality")
                        +"agent"(E2, Host)
                        +"target"(E2, Guest)
                        +"value"(Host, "communalism")
                    }.meaning {
                        +"$Host, who values communalism, showed hospitality to $Guest"
                    }

                    +event(E3) {
                        +"tag"(E3, "harm")
                        +"type"(E3, HarmType)
                        +"agent"(E3, Host)
                        +"target"(E3, Guest)
                    }.meaning {
                        +"However, $Host harmed $Guest ($HarmType), violating the norms of hospitality"
                    }

                    -event(eMid) {
                        +"type"(eMid, "leaveTown")
                        +"agent"(eMid, Guest)
                        between(E1, E3)
                    }.meaning {
                        +"$Guest did not leave town in the meantime"
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

            val logger = NarrativeGenerationLogger.create("PatternMatcher", LoggingConfig())

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
                        .add(Clause.of(Struct.of("value", Atom.of(mira.name), Atom.of("communalism"))))
                        .let { kb ->
                            patterns.fold(kb) { currentKb, pattern ->
                                currentKb.addAll(pattern.clauses.mapNotNull { it.rule })
                            }
                        }

                val eventProcessor = IncrementalEventProcessor(kb, patterns, logger = logger)
                val finalProcessor = eventProcessor.matchAll(events)

                finalProcessor.eventMatchResults.last().activePartialMatches shouldBe 0

                val patternMatches = finalProcessor.matcher.completedMatches
                patternMatches.size shouldBe 1

                patternMatches.first().getCompiledDescription() shouldBe
                    """
                    'Emin' entered the town
                    'Mira', who values communalism, showed hospitality to 'Emin'
                    However, 'Mira' harmed 'Emin' (steal), violating the norms of hospitality
                    'Emin' did not leave town in the meantime
                    """.trimIndent()
            }
        }
    })
