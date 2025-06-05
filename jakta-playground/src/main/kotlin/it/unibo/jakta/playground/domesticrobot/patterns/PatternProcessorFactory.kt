package it.unibo.jakta.playground.domesticrobot.patterns

import it.unibo.jakta.agents.bdi.narrativegenerator.StopWatch
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig.Companion.defaultConfig
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.matcher.IncrementalEventProcessor
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern

object PatternProcessorFactory {
    val createEventProcessor: (
        List<SiftingPattern>,
        NarrativeGenerationLogger?,
        StopWatch?,
    ) -> IncrementalEventProcessor =
        { patterns, logger, stopwatch ->
            val config = defaultConfig
            val converter = EventSerializer.of(config)

            val kb =
                KnowledgeBase.empty(converter).let { kb ->
                    patterns.fold(kb) { currentKb, pattern ->
                        currentKb.addAll(pattern.clauses.mapNotNull { it.rule })
                    }
                }

            IncrementalEventProcessor(
                kb,
                patterns,
                stopwatch,
                logger,
            )
        }
}
