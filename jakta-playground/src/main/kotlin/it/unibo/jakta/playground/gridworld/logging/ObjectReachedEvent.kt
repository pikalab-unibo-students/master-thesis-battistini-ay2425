package it.unibo.jakta.playground.gridworld.logging

import it.unibo.jakta.agents.bdi.engine.logging.events.EnvironmentEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ObjectReached")
data class ObjectReachedEvent(
    val objectName: String,
    override val description: String?,
) : EnvironmentEvent {
    constructor(objectName: String) : this(objectName, "Reached object $objectName")
}
