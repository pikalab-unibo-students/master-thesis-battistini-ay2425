package it.unibo.jakta.playground.experiment

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import it.unibo.jakta.agents.bdi.engine.Mas
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import java.util.UUID

abstract class AbstractApplication : CliktCommand() {
    private val urlRegex = "^(https?|tcp)://([\\w.-]+)(:\\d+)?(/.*)?$".toRegex()

    val logToFile: Boolean by option()
        .flag()
        .help("Whether to output logs to the local filesystem.")

    val logToConsole: Boolean by option()
        .flag(default = true)
        .help("Whether to output log to std output.")

    val logToServer: Boolean by option()
        .flag()
        .help("Whether to output logs to a log server.")

    val logToSingleFile: Boolean by option()
        .flag()
        .help("Whether to output logs to a single file.")

    val logLevel: Log4jLevel by option()
        .enum<Log4jLevel>()
        .default(DEFAULT_LOG_LEVEL)
        .help("The minimum log level")

    val logDir: String by option()
        .default(DEFAULT_LOG_DIR)
        .help("Whether to output log to the local filesystem.")

    val logServerUrl: String by option()
        .default(LoggingConfig.LOG_SERVER_URL)
        .help("Url of the server where logs are sent.")
        .check("value must be a valid URL") { it.matches(urlRegex) }

    override fun run() {
        println("Experiment started")
        val runId = UUID.randomUUID().toString()

        val logConfig = createLoggingConfig(runId)
        println(logConfig)

        createMas(logConfig).start()

        println("Logging execution trace to $runId")
    }

    abstract fun createMas(logConfig: LoggingConfig): Mas

    abstract fun createLoggingConfig(expName: String): LoggingConfig

    companion object {
        const val DEFAULT_LOG_DIR = "logs"
        val DEFAULT_LOG_LEVEL = Log4jLevel.INFO
    }
}
