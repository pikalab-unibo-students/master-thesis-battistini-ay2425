package it.unibo.jakta.agents.bdi.narrativegenerator.serialization

import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.Single

@Single
class NarrativeGenerationJsonModule : SerializersModuleProvider {
    override val modules =
        SerializersModule {
            polymorphic(LogEvent::class) {
                subclass(PatternMatchLogEvent::class)
            }
        }
}
