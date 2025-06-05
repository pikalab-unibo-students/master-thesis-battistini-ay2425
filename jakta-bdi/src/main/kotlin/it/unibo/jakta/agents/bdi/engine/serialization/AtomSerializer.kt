package it.unibo.jakta.agents.bdi.engine.serialization

import it.unibo.tuprolog.core.Atom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive

object AtomSerializer : KSerializer<Atom> {
    override val descriptor =
        buildClassSerialDescriptor("Atom") {
            element<String>("value")
        }

    override fun serialize(
        encoder: Encoder,
        value: Atom,
    ) {
        require(encoder is JsonEncoder) { "This serializer can only be used with JSON" }
        val jsonPrimitive = JsonPrimitive(value.value) // completeName
        encoder.encodeJsonElement(jsonPrimitive)
    }

    override fun deserialize(decoder: Decoder): Atom {
        require(decoder is JsonDecoder) { "This serializer can only be used with JSON" }

        val element = decoder.decodeJsonElement()
        require(element is JsonPrimitive) { "Expected JsonPrimitive for Atom deserialization" }

        val valueAtom = element.content
        val atom = Atom.of(valueAtom)

        return atom
    }
}
