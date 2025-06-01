package it.unibo.jakta.agents.bdi.narrativegenerator.matcher

import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Substitution

data class PatternMatch(
    val pattern: SiftingPattern,
    val bindings: Substitution,
) {
    fun getCompiledDescription(): String {
        var d = pattern.description
        bindings.forEach { (key, value) ->
            d = d.replace("${key.name}_${key.id}", value.toString())
        }
        return d
    }
}
