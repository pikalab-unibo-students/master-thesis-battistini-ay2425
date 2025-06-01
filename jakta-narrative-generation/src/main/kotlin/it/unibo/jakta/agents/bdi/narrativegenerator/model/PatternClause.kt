package it.unibo.jakta.agents.bdi.narrativegenerator.model

import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializableRule
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializableStruct
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializableVar
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase.Companion.termFormatter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("PatternClause")
data class PatternClause(
    val eventVar: SerializableVar,
    val purpose: String? = null,
    val constraints: List<SerializableStruct>,
    @Transient
    val rule: SerializableRule? = null,
    val isUnlessClause: Boolean = false,
) {
    override fun toString(): String {
        val constraintsStr = constraints.joinToString(",\n    ") { termFormatter.format(it) }
        val format = termFormatter.format(eventVar)
        return "(${if (isUnlessClause) "unless event" else "event"} $format where\n    $constraintsStr)"
    }
}
