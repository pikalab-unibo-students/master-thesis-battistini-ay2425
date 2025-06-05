package it.unibo.jakta.agents.bdi.engine.logging

import org.apache.logging.log4j.Level

data class LoggingConfig(
    val logServerURL: String = LOG_SERVER_URL,
    val logToFile: Boolean = LOG_TO_FILE,
    val logToConsole: Boolean = LOG_TO_CONSOLE,
    val logToServer: Boolean = LOG_TO_SERVER,
    val logLevel: Level = Level.INFO,
    val logDir: String = LOG_DIR,
    val logToSingleFile: Boolean = LOG_TO_SINGLE_FILE,
) {
    companion object {
        const val LOG_SERVER_URL = "tcp://localhost:5044"
        const val LOG_TO_FILE = false
        const val LOG_TO_CONSOLE = true
        const val LOG_TO_SINGLE_FILE = false
        const val LOG_TO_SERVER = false
        const val LOG_DIR = "logs"
        val LOG_LEVEL: Level = Level.INFO
    }
}
