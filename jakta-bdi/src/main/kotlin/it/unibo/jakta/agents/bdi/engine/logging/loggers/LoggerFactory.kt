package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.appenders.Appenders.buildAppenders
import org.apache.logging.log4j.Logger

class LoggerFactory private constructor(
    private val name: String,
    private val logFileName: String,
    private val loggingConfig: LoggingConfig,
) {
    val logger: Logger by lazy {
        val appenders = buildAppenders(name, logFileName, loggingConfig)
        LoggerConfigurator.addLogger(name, loggingConfig.logLevel, appenders)
        JaktaLogger.logger(name)
    }

    companion object {
        fun create(
            prefix: String,
            id: String,
            loggingConfig: LoggingConfig,
        ): LoggerFactory {
            val name = "$prefix-$id"
            val logFileName =
                if (loggingConfig.logToSingleFile) {
                    "${loggingConfig.logDir}${separator}trace-$id"
                } else {
                    "${loggingConfig.logDir}${separator}$name"
                }
            return LoggerFactory(name, logFileName, loggingConfig)
        }
    }
}
