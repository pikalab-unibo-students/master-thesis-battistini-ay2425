package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger.Companion.implementation
import org.apache.logging.log4j.Logger

class MasLogger(
    val masID: MasID,
    private val delegate: LoggerFactory,
) : JaktaLogger {
    override val logger: Logger get() = delegate.logger

    override fun log(event: () -> LogEvent) = logger.implementation(masID, event)

    companion object {
        fun create(
            masID: MasID,
            loggingConfig: LoggingConfig,
        ): MasLogger {
            val delegate = LoggerFactory.create("Mas", masID.id, loggingConfig)
            return MasLogger(masID, delegate)
        }
    }
}
