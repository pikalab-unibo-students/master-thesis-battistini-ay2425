package it.unibo.jakta.agents.bdi.narrativegenerator.dsl

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.PatternConverter
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.dsl.LogicProgrammingScope

@PatternDsl
class PatternScope(
    val name: String,
    val converter: PatternConverter = PatternConverter.of(),
) : LogicProgrammingScope by LogicProgrammingScope.empty() {
    val eventClauses = mutableListOf<PatternClause>()
    val rules = mutableListOf<Rule>()
    var description = ""

    fun event(
        eventVar: Var = Var.anonymous(),
        f: ConstraintScope.() -> Unit,
    ): PatternClause = ConstraintScope(eventVar).also(f).build()

    override fun rule(function: LogicProgrammingScope.() -> Any): Rule = super.rule(function).also { rule(it) }

    fun rule(rule: Rule) {
        rules += rule
    }

    operator fun PatternClause.unaryPlus() {
        eventClauses.add(this)
    }

    operator fun PatternClause.unaryMinus() {
        eventClauses.add(this.copy(isUnlessClause = true))
    }

    fun build(): SiftingPattern {
        val description =
            description.ifBlank {
                eventClauses.mapNotNull { it.purpose }.joinToString("\n")
            }
        val basePattern = SiftingPattern(name, description, eventClauses)
        return converter.convert(basePattern)
    }
}
