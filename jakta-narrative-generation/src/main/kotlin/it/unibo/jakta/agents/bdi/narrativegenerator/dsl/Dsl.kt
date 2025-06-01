package it.unibo.jakta.agents.bdi.narrativegenerator.dsl

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.PatternConverter
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Var

@DslMarker
annotation class PatternDsl

fun pattern(
    name: String,
    converter: PatternConverter = PatternConverter.of(),
    f: PatternScope.() -> Unit,
): SiftingPattern = PatternScope(name, converter).also(f).build()

fun event(
    eventVar: Var = Var.anonymous(),
    f: ConstraintScope.() -> Unit,
): PatternClause = ConstraintScope(eventVar).also(f).build()
