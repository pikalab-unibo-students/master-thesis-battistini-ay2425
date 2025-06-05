package it.unibo.jakta.agents.bdi.engine.logging.loggers.appenders

import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.CONSOLE_APPENDER_NAME
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.TCP_APPENDER_NAME
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.addFileAppender
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.addTcpAppender
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.jsonLayout
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator.logFilePatternLayout
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig

object Appenders {
    fun buildAppenders(
        appenderName: String,
        logFilePath: String,
        loggingConfig: LoggingConfig,
    ): List<String> {
        val appenders = mutableListOf<String>()

        if (loggingConfig.logToConsole) {
            appenders.add(CONSOLE_APPENDER_NAME)
        }

        if (loggingConfig.logToFile) {
            val logFileAppenderName = "$appenderName-LogFile"
            val jsonFileAppenderName = "$appenderName-JsonLinesFile"

            addFileAppender(
                logFileAppenderName,
                "$logFilePath.log",
                logFilePatternLayout,
            )
            appenders.add(logFileAppenderName)

            addFileAppender(
                jsonFileAppenderName,
                "$logFilePath.jsonl",
                jsonLayout,
            )
            appenders.add(jsonFileAppenderName)
        }

        if (loggingConfig.logToServer) {
            addTcpAppender(
                loggingConfig.logServerURL,
                jsonLayout,
            )
            appenders.add(TCP_APPENDER_NAME)
        }

        return appenders
    }
}
