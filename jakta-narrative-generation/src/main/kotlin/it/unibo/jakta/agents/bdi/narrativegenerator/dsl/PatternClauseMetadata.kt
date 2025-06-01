package it.unibo.jakta.agents.bdi.narrativegenerator.dsl

import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause

object PatternClauseMetadata {
    class PatternClauseContextScope {
        val strings = mutableListOf<String>()

        operator fun String.unaryPlus() {
            strings += this
        }

        fun build(): String = strings.joinToString()
    }

    fun PatternClause.meaning(f: PatternClauseContextScope.() -> Unit): PatternClause {
        val purpose = PatternClauseContextScope().also(f).build()
        return copy(purpose = purpose)
    }
}
