package it.unibo.jakta.agents.bdi.narrativegenerator.dsl

import it.unibo.jakta.agents.bdi.narrativegenerator.model.PatternClause
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.dsl.LogicProgrammingScope

@PatternDsl
class ConstraintScope(
    private val eventVar: Var,
    private val lpScope: LogicProgrammingScope = LogicProgrammingScope.empty(),
) : LogicProgrammingScope by lpScope {
    private val patternConstraints = mutableListOf<Struct>()

    operator fun String.unaryPlus() {
        patternConstraints.add(Atom.of(this))
    }

    operator fun Struct.unaryPlus() {
        patternConstraints.add(this)
    }

    fun between(
        start: Term,
        end: Term,
    ) {
        val startTime = Var.of("StartTime")
        val endTime = Var.of("EndTime")
        val time = Var.of("Time")

        val structs =
            listOf(
                Struct.of("time", eventVar, time),
                Struct.of("time", start, startTime),
                Struct.of("time", end, endTime),
                Struct.of(">", time, startTime),
                Struct.of("<", time, endTime),
            )

        patternConstraints.addAll(structs)
    }

    fun build() = PatternClause(eventVar, constraints = patternConstraints)
}
