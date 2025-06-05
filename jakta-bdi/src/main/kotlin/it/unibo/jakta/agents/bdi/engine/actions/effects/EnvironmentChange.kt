package it.unibo.jakta.agents.bdi.engine.actions.effects

import it.unibo.jakta.agents.bdi.engine.Agent
import it.unibo.jakta.agents.bdi.engine.logging.events.EnvironmentEvent
import it.unibo.jakta.agents.bdi.engine.messages.Message
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("EnvironmentChange")
sealed interface EnvironmentChange :
    SideEffect,
    EnvironmentEvent

@Serializable
@SerialName("SpawnAgent")
data class SpawnAgent(
    val agent: Agent,
    override val description: String?,
) : EnvironmentChange {
    constructor(agent: Agent) : this(
        agent,
        "Agent ${agent.name} has been spawned in the environment",
    )
}

@Serializable
@SerialName("RemoveAgent")
data class RemoveAgent(
    val agentName: String,
    override val description: String?,
) : EnvironmentChange {
    constructor(agentName: String) : this(
        agentName,
        "Agent $agentName has been removed from the environment",
    )
}

@Serializable
@SerialName("SendMessage")
data class SendMessage(
    val message: Message,
    val recipient: String,
    override val description: String?,
) : EnvironmentChange {
    constructor(message: Message, recipient: String) : this(
        message,
        recipient,
        "Sent message to $recipient: ${message.type.javaClass.simpleName.lowercase()} ${message.value}",
    )
}

@Serializable
@SerialName("BroadcastMessage")
data class BroadcastMessage(
    val message: Message,
    override val description: String?,
) : EnvironmentChange {
    constructor(message: Message) : this(
        message,
        "Agent ${message.from} broadcast message ${message.type.javaClass.simpleName}" +
            "\n\tto all agents" +
            "\n\twith content: ${message.value}",
    )
}

@Serializable
@SerialName("PopMessage")
data class PopMessage(
    val agentName: String,
    override val description: String?,
) : EnvironmentChange {
    constructor(agentName: String) : this(
        agentName,
        "Popped a message from the message queue of agent $agentName",
    )
}

@Serializable
@SerialName("AddData")
data class AddData(
    val key: String,
    @Contextual
    val value: Any,
    override val description: String?,
) : EnvironmentChange {
    constructor(key: String, value: Any) : this(
        key,
        value,
        "Key-value $key=$value has been added to the environment",
    )
}

@Serializable
@SerialName("RemoveData")
data class RemoveData(
    val key: String,
    override val description: String?,
) : EnvironmentChange {
    constructor(key: String) : this(
        key,
        "Key-value $key has been removed from the environment",
    )
}

@Serializable
@SerialName("UpdateData")
data class UpdateData(
    val newData: Map<String, @Contextual Any>,
    override val description: String?,
) : EnvironmentChange {
    constructor(newData: Map<String, @Contextual Any>) : this(
        newData,
        "Environment has been updated with the new data: $newData",
    )
}
