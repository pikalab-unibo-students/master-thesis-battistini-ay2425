package it.unibo.jakta.agents.bdi.narrativegenerator.converter

import it.unibo.jakta.agents.bdi.engine.Jakta.removeSource
import it.unibo.jakta.agents.bdi.engine.Jakta.source
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.engine.logging.events.EventType
import it.unibo.jakta.agents.bdi.engine.messages.Achieve
import it.unibo.jakta.agents.bdi.engine.messages.MessageType
import it.unibo.jakta.agents.bdi.engine.messages.Tell
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer.Companion.structOfEvent
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import kotlin.reflect.KClass

data class EventSerializerConfig(
    val customMappings: Map<KClass<*>, (Any, String, List<String>) -> List<Struct>> = emptyMap(),
    val ignoredTypes: Set<KClass<*>> = emptySet(),
    val flattenObjects: Boolean = DEFAULT_FLATTEN_OBJECTS,
    val maxDepth: Int = DEFAULT_MAX_DEPTH,
) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> withTermMapping(
        type: KClass<T>,
        mapper: (T, String, List<String>) -> List<Struct>,
    ): EventSerializerConfig =
        copy(customMappings = this.customMappings + (type to { value, id, path -> mapper(value as T, id, path) }))

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> withSimpleMapping(
        type: KClass<T>,
        mapper: (T) -> String,
    ): EventSerializerConfig {
        val termMapper: (Any, String, List<String>) -> List<Struct> = { value, id, path ->
            listOf(structOfEvent(id, path, Atom.of(mapper(value as T))))
        }
        return copy(customMappings = this.customMappings + (type to termMapper))
    }

    companion object {
        const val DEFAULT_FLATTEN_OBJECTS = true
        const val DEFAULT_MAX_DEPTH = 5

        val defaultConfig =
            EventSerializerConfig().let {
                it
                    .withTermMapping(Belief::class) { belief, id, path ->
                        listOf(
                            structOfEvent(id, path + "source", Atom.of(belief.source())),
                            structOfEvent(id, path + "content", belief.removeSource()),
                            structOfEvent(id, path + "rule", belief.rule.head),
                        )
                    }.withSimpleMapping(MessageType::class) { messageType ->
                        when (messageType) {
                            is Achieve -> "achieve"
                            is Tell -> "tell"
                        }
                    }.withSimpleMapping(ContextUpdate::class) { cu ->
                        cu.name
                    }.withTermMapping(EventType::class) { eventType, id, path ->
                        listOf(structOfEvent(id, "type", Atom.of(eventType.type)))
                    }
            }
    }
}
