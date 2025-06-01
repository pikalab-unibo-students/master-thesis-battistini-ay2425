package it.unibo.jakta.agents.bdi.narrativegenerator.converter

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.impl.EventSerializerImpl
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

interface EventSerializer {
    fun convert(
        eventId: String,
        event: Any,
    ): List<Struct>

    companion object {
        fun of(config: EventSerializerConfig): EventSerializer {
            val config =
                config.withTermMapping(AgentID::class) { agentId, id, path ->
                    listOf(structOfEvent(id, path.joinToString("_"), Atom.of(agentId.name)))
                }
            return EventSerializerImpl(config)
        }

        fun structOfEvent(
            id: String,
            functor: String,
            value: Term,
        ): Struct = Struct.of(functor, Atom.of(id), value)

        fun structOfEvent(
            id: String,
            functor: List<String>,
            value: Term,
        ): Struct {
            val functorName = functor.joinToString("_")
            return Struct.of(functorName, Atom.of(id), value)
        }
    }
}
