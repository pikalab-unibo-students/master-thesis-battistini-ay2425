package it.unibo.jakta.agents.bdi.narrativegenerator.converter.impl

import it.unibo.jakta.agents.bdi.narrativegenerator.converter.PatternConverter
import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct

internal class PatternConverterImpl : PatternConverter {
    override fun convert(pattern: SiftingPattern): SiftingPattern {
        val updatedClauses =
            pattern.clauses.mapIndexed { index, clause ->
                val rules = clause.toProlog(index, pattern.name)
                clause.copy(rule = rules)
            }
        return pattern.copy(clauses = updatedClauses)
    }

    private fun PatternClause.toProlog(
        index: Int,
        patterName: String,
    ): Rule {
        val varsFromConstraints = constraints.flatMap { it.variables }

        val head =
            if (this.isUnlessClause) {
                "unless_event_${index}_$patterName"
            } else {
                "event_${index}_$patterName"
            }

        val vars = varsFromConstraints.distinct()
        val body = constraints

        val ruleHead = Struct.of(head, vars)
        return Rule.of(ruleHead, body)
    }
}
