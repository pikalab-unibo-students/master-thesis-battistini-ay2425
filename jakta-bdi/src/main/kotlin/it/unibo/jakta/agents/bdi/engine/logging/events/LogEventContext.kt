package it.unibo.jakta.agents.bdi.engine.logging.events

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.plangeneration.PgpID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A wrapper class for Jakta log events.
 *
 * This class wraps a `JaktaLogEvent` instance and includes optional identifiers for the multi-agent system (MAS),
 * the agent, and the plan generation procedure (PGP) associated with the event.
 *
 * @property event The core log event to be wrapped. The type of event may vary and is represented by the `JaktaLogEvent` interface.
 * @property masID The optional identifier of the multi-agent system in which the event occurred.
 * @property agentID The optional identifier of the agent associated with the event.
 * @property pgpID The optional identifier of the plan generation procedure associated with the event.
 */
@Serializable
@SerialName("LogEvent")
data class LogEventContext(
    val event: LogEvent,
    val masID: MasID? = null,
    val agentID: AgentID? = null,
    val pgpID: PgpID? = null,
)
