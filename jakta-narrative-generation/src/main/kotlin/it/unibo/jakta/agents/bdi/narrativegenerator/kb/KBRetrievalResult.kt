package it.unibo.jakta.agents.bdi.narrativegenerator.kb

import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate.REMOVAL
import it.unibo.tuprolog.core.Clause

data class KBRetrievalResult(
    val modifiedClauses: List<ClauseUpdate>,
    val updatedKB: KnowledgeBase,
)

data class ClauseUpdate(
    val clause: Clause,
    val updateType: ContextUpdate,
) {
    companion object {
        fun removal(clause: Clause) = ClauseUpdate(clause, REMOVAL)

        fun addition(clause: Clause) = ClauseUpdate(clause, ADDITION)
    }
}
