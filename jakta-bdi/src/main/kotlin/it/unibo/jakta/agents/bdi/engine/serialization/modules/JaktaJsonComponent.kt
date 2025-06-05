package it.unibo.jakta.agents.bdi.engine.serialization.modules

import it.unibo.jakta.agents.bdi.engine.depinjection.IsolatedKoinComponent
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature
import kotlinx.serialization.Contextual
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import kotlin.getValue

typealias SerializableTerm = @Contextual Term
typealias SerializableVar = @Contextual Var
typealias SerializableStruct = @Contextual Struct
typealias SerializableRule = @Contextual Rule
typealias SerializableSignature = @Contextual Signature

object JaktaJsonComponent : IsolatedKoinComponent {
    @JvmStatic
    val json: Json by inject()
}
