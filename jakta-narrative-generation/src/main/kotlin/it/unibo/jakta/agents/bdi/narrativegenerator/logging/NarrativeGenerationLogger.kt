package it.unibo.jakta.agents.bdi.narrativegenerator.logging

import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger
import it.unibo.jakta.agents.bdi.engine.logging.loggers.LoggerFactory
import org.apache.logging.log4j.Logger

class NarrativeGenerationLogger(
    private val delegate: LoggerFactory,
) : JaktaLogger {
    override val logger: Logger get() = delegate.logger

    override fun log(event: () -> LogEvent) = logger.info(event)

    companion object {
        fun create(
            name: String,
            loggingConfig: LoggingConfig,
        ): NarrativeGenerationLogger {
            val delegate = LoggerFactory.create("Mas", name, loggingConfig)
            return NarrativeGenerationLogger(delegate)
        }
    }
}
