package it.unibo.jakta.playground.gridworld.serialization

import it.unibo.jakta.agents.bdi.engine.logging.events.JaktaLogEvent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.playground.gridworld.logging.ObjectReachedEvent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.Single

@Single
class GridWorldJsonModule : SerializersModuleProvider {
    override val modules =
        SerializersModule {
            polymorphic(JaktaLogEvent::class) {
                subclass(ObjectReachedEvent::class)
            }
        }
}
