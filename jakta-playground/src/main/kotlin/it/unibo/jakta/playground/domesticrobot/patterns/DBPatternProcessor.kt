package it.unibo.jakta.playground.domesticrobot.patterns

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig.Companion.defaultConfig
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.matcher.IncrementalEventProcessor
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern

object DBPatternProcessor {
    val createEventProcessor: (List<SiftingPattern>) -> IncrementalEventProcessor = { patterns ->
        val config = defaultConfig
        val converter = EventSerializer.of(config)

        val baseKB =
            KnowledgeBase.empty(converter).let { kb ->
                patterns.fold(kb) { currentKb, pattern ->
                    currentKb.addAll(pattern.clauses.mapNotNull { it.rule })
                }
            }

        val eventKB = KnowledgeBase.empty(converter)
        IncrementalEventProcessor(baseKB, eventKB, patterns)
    }
}
