package it.unibo.jakta.agents.bdi.engine.serialization

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

object TermSerializer : KSerializer<Term> {
    override val descriptor =
        buildClassSerialDescriptor("Term") {
            element<String>("type")
            element<JsonElement>("value")
        }

    override fun serialize(
        encoder: Encoder,
        value: Term,
    ) {
        require(encoder is JsonEncoder) { "This serializer can only be used with JSON" }

        val (type, serializedValue) =
            when (value) {
                is Atom -> "Atom" to serializeWithSerializer(encoder, AtomSerializer, value)
                is Var -> "Var" to serializeWithSerializer(encoder, VarSerializer, value)
                is Struct -> "Struct" to serializeWithSerializer(encoder, StructSerializer, value)
                else -> "Fallback" to serializeWithFallback(encoder, value)
            }

        val jsonObject =
            buildJsonObject {
                put("type", JsonPrimitive(type))
                put("value", serializedValue)
            }

        encoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): Term {
        require(decoder is JsonDecoder) { "This serializer can only be used with JSON" }

        val element = decoder.decodeJsonElement()
        require(element is JsonObject) { "Expected JsonObject for Term deserialization" }

        val type =
            element["type"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing type field")
        val valueElement =
            element["value"]
                ?: throw IllegalArgumentException("Missing value field")

        return when (type) {
            "Atom" -> deserializeWithSerializer(decoder, AtomSerializer, valueElement)
            "Var" -> deserializeWithSerializer(decoder, VarSerializer, valueElement)
            "Struct" -> deserializeWithSerializer(decoder, StructSerializer, valueElement)
            "Rule" -> deserializeWithSerializer(decoder, RuleSerializer, valueElement)
            "Fallback" -> deserializeWithFallback(decoder, valueElement) as Term
            else -> throw IllegalArgumentException("Unknown term type: $type")
        }
    }

    private fun <T> serializeWithSerializer(
        encoder: JsonEncoder,
        serializer: SerializationStrategy<T>,
        value: T,
    ): JsonElement = encoder.json.encodeToJsonElement(serializer, value)

    private fun serializeWithFallback(
        encoder: JsonEncoder,
        value: Any,
    ): JsonElement = encoder.json.encodeToJsonElement(FallbackSerializer, value)

    private fun <T> deserializeWithSerializer(
        decoder: JsonDecoder,
        serializer: DeserializationStrategy<T>,
        element: JsonElement,
    ): T = decoder.json.decodeFromJsonElement(serializer, element)

    private fun deserializeWithFallback(
        decoder: JsonDecoder,
        element: JsonElement,
    ): Any = decoder.json.decodeFromJsonElement(FallbackSerializer, element)
}
