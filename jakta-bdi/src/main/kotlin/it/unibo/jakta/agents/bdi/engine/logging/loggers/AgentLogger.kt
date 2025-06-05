package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger.Companion.implementation
import org.apache.logging.log4j.Logger

class AgentLogger(
    val masID: MasID,
    val agentID: AgentID,
    private val delegate: LoggerFactory,
) : JaktaLogger {
    override val logger: Logger get() = delegate.logger

    override fun log(event: () -> LogEvent) = logger.implementation(masID, event, agentID)

    companion object {
        fun create(
            masID: MasID,
            agentID: AgentID,
            agentName: String,
            loggingConfig: LoggingConfig,
        ): AgentLogger {
            val id = if (loggingConfig.logToSingleFile) masID.id else "${masID.id}-${agentID.id}"
            val delegate =
                LoggerFactory.create(
                    agentName,
                    id,
                    loggingConfig,
                )
            return AgentLogger(masID, agentID, delegate)
        }
    }
}
