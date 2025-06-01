package it.unibo.jakta.agents.bdi.narrativegenerator.converter.impl

import it.unibo.jakta.agents.bdi.narrativegenerator.NarrativeGenerator.toSnakeCase
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer.Companion.structOfEvent
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import kotlin.collections.iterator
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties

internal class EventSerializerImpl(
    val config: EventSerializerConfig = EventSerializerConfig(),
) : EventSerializer {
    override fun convert(
        eventId: String,
        event: Any,
    ): List<Struct> = processObject(eventId, event, config, emptyList(), 0)

    private fun processObject(
        id: String,
        obj: Any,
        config: EventSerializerConfig,
        pathPrefix: List<String>,
        depth: Int,
    ): List<Struct> {
        if (depth > config.maxDepth) {
            return emptyList()
        }

        val terms = mutableListOf<Struct>()
        val properties = obj::class.memberProperties

        for (property in properties) {
            try {
                @Suppress("UNCHECKED_CAST")
                val value = (property as KProperty1<Any, *>).get(obj)

                if (value == null) continue

                val fieldName = property.name.toSnakeCase()
                val currentPath = pathPrefix + fieldName

                terms.addAll(processValue(id, value, config, currentPath, depth + 1))
            } catch (_: IllegalAccessException) {
                // Skip properties we can't access
                continue
            } catch (_: kotlin.reflect.full.IllegalCallableAccessException) {
                // Skip properties we can't access due to visibility
                continue
            }
        }

        return terms
    }

    private fun processValue(
        id: String,
        value: Any,
        config: EventSerializerConfig,
        path: List<String>,
        depth: Int,
    ): List<Struct> =
        when {
            findCustomMapping(value, config.customMappings) != null -> {
                findCustomMapping(value, config.customMappings)!!(value, id, path)
            }

            isIgnoredType(value, config.ignoredTypes) -> emptyList()

            isPrimitiveType(value) -> listOf(structOfEvent(id, path, convertPrimitiveToTerm(value)))

            value is Collection<*> -> processCollection(id, value, config, path, depth)

            value is Map<*, *> -> processMap(id, value, config, path, depth)

            value is Struct -> listOf(structOfEvent(id, path, value))

            config.flattenObjects -> processObject(id, value, config, path, depth)

            else -> emptyList()
        }

    private fun processCollection(
        id: String,
        collection: Collection<*>,
        config: EventSerializerConfig,
        path: List<String>,
        depth: Int,
    ): List<Struct> {
        val terms = mutableListOf<Struct>()

        collection.forEach { item ->
            if (item != null) {
                terms.addAll(processValue(id, item, config, path, depth))
            }
        }

        return terms
    }

    private fun processMap(
        id: String,
        map: Map<*, *>,
        config: EventSerializerConfig,
        path: List<String>,
        depth: Int,
    ): List<Struct> {
        val terms = mutableListOf<Struct>()

        for ((key, value) in map) {
            if (key != null && value != null) {
                val keyPath = path + key.toString().toSnakeCase()
                terms.addAll(processValue(id, value, config, keyPath, depth))
            }
        }

        return terms
    }

    private fun findCustomMapping(
        value: Any,
        mappings: Map<KClass<*>, (Any, String, List<String>) -> List<Struct>>,
    ): ((Any, String, List<String>) -> List<Struct>)? {
        val valueClass = value::class

        // First check exact class match
        mappings[valueClass]?.let { return it }

        // Then check all mapping classes to see if any is a supertype of valueClass
        for ((mappingClass, mappingFunction) in mappings) {
            if (mappingClass.isSuperclassOf(valueClass)) {
                return mappingFunction
            }
        }

        return null
    }

    private fun isIgnoredType(
        value: Any,
        ignoredTypes: Set<KClass<*>>,
    ): Boolean {
        val valueClass = value::class

        // First check exact class match
        if (ignoredTypes.contains(valueClass)) return true

        // Then check all supertypes
        for (supertype in valueClass.supertypes) {
            val classifier = supertype.classifier as? KClass<*>
            if (classifier != null && ignoredTypes.contains(classifier)) {
                return true
            }
        }

        return false
    }

    private fun isPrimitiveType(value: Any): Boolean =
        when (value) {
            is String, is Int, is Long, is Double, is Float, is Boolean, is Char -> true
            else -> false
        }

    private fun convertPrimitiveToTerm(value: Any): Term =
        when (value) {
            is String -> Atom.of(value)
            is Number -> Numeric.of(value)
            is Boolean -> Atom.of(value.toString())
            is Char -> Atom.of(value.toString())
            else -> Atom.of(value.toString())
        }
}
