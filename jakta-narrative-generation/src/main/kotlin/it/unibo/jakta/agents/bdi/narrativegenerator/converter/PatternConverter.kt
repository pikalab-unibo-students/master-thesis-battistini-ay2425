package it.unibo.jakta.agents.bdi.narrativegenerator.converter

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.impl.PatternConverterImpl
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern

interface PatternConverter {
    fun convert(pattern: SiftingPattern): SiftingPattern

    companion object {
        fun of(): PatternConverter = PatternConverterImpl()
    }
}
