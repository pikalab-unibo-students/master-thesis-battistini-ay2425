package it.unibo.jakta.agents.bdi.dsl.logging

import it.unibo.jakta.agents.bdi.dsl.ScopeBuilder
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_DIR
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_LEVEL
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_SERVER_URL
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_TO_CONSOLE
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_TO_FILE
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_TO_SERVER
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig.Companion.LOG_TO_SINGLE_FILE
import org.apache.logging.log4j.Level

class LoggingConfigScope : ScopeBuilder<LoggingConfig> {
    var logServerUrl: String = LOG_SERVER_URL
    var logDir: String = LOG_DIR
    var logToFile: Boolean = LOG_TO_FILE
    var logToConsole: Boolean = LOG_TO_CONSOLE
    var logToServer: Boolean = LOG_TO_SERVER
    var logToSingleFile: Boolean = LOG_TO_SINGLE_FILE
    var logLevel: Level = LOG_LEVEL

    fun debug() {
        logLevel = Level.DEBUG
    }

    fun info() {
        logLevel = Level.INFO
    }

    fun warn() {
        logLevel = Level.WARN
    }

    fun error() {
        logLevel = Level.ERROR
    }

    override fun build(): LoggingConfig =
        LoggingConfig(
            logServerURL = logServerUrl,
            logToFile = logToFile,
            logToConsole = logToConsole,
            logToServer = logToServer,
            logToSingleFile = logToSingleFile,
            logLevel = logLevel,
            logDir = logDir,
        )
}
