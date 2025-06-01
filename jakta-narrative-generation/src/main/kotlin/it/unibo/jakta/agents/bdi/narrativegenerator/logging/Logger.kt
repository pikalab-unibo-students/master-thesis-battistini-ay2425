package it.unibo.jakta.agents.bdi.narrativegenerator.logging

import it.unibo.jakta.agents.bdi.engine.logging.LoggerFactory
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.appenders.Appenders.buildAppenders
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

interface NarrativeGenerationLogger {
    val logger: Logger

    fun trace(message: () -> Any?) = logger.trace(message)

    fun debug(message: () -> Any?) = logger.debug(message)

    fun info(message: () -> Any?) = logger.info(message)

    fun warn(message: () -> Any?) = logger.warn(message)

    fun error(message: () -> Any?) = logger.error(message)

    companion object {
        fun logger(name: String): Logger = LogManager.getLogger(name)

        fun of(
            name: String,
            loggingConfig: LoggingConfig,
        ): NarrativeGenerationLogger {
            val level = loggingConfig.logLevel
            val appenders = buildAppenders(name, loggingConfig)

            LoggerFactory.addLogger(name, level, appenders)
            val logger = logger(name)
            return NarrativeGenerationLoggerImpl(logger)
        }
    }
}

class NarrativeGenerationLoggerImpl(
    override val logger: Logger,
) : NarrativeGenerationLogger
