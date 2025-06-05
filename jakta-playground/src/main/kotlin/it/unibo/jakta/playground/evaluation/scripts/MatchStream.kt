package it.unibo.jakta.playground.evaluation.scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent.json
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import it.unibo.jakta.playground.domesticrobot.patterns.PatternProcessorFactory.createEventProcessor
import it.unibo.jakta.playground.domesticrobot.patterns.SiftingPatterns.domesticRobotPatterns
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.message.ObjectMessage
import org.koin.ksp.generated.module

class LogEventsStreamProcessor : CliktCommand() {
    private val inputHost: String by option()
        .default(DEFAULT_INPUT_HOST)
        .help("Host address for input connection")

    private val inputPort: Int by option()
        .int()
        .default(DEFAULT_INPUT_PORT)
        .help("Port for input connection")

    private val outputHost: String by option()
        .default(DEFAULT_OUTPUT_HOST)
        .help("Host address for output connection")

    private val outputPort: Int by option()
        .int()
        .default(DEFAULT_OUTPUT_PORT)
        .help("Port for output connection")

    private val verbose: Boolean by option("--verbose", "-v")
        .flag()
        .help("Enable verbose logging")

    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)
    }

    override fun run(): Unit =
        runBlocking {
            val loggingConfig =
                LoggingConfig(
                    logToServer = true,
                    logServerURL = "tcp://$outputHost:$outputPort",
                )
            val logger = NarrativeGenerationLogger.create("StreamProcessor", loggingConfig)

            println("Starting LogEntry stream processor")
            println("Input: $inputHost:$inputPort")
            println("Output: $outputHost:$outputPort")

            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).tcp().connect(inputHost, inputPort)
            val receiveChannel = socket.openReadChannel()

            processSequentially(receiveChannel, logger)
        }

    private suspend fun processSequentially(
        receiveChannel: ByteReadChannel,
        logger: NarrativeGenerationLogger,
    ) {
        var processor = createEventProcessor(domesticRobotPatterns, null, null)
        var lineCount = 0
        var errorCount = 0
        var matchCount = 0

        while (true) {
            val line = receiveChannel.readUTF8Line()
            if (verbose) {
                if (line != null) {
                    println("Received line: $line")
                } else {
                    println("Received empty line")
                }
            }

            if (line != null) {
                lineCount++
                val logEntry =
                    try {
                        json.decodeFromString<LogEntry>(line)
                    } catch (e: Exception) {
                        errorCount++
                        if (verbose) {
                            println("Error parsing line $lineCount: ${e.message}")
                        }
                        null
                    }

                if (logEntry != null) {
                    val previousProcessor = processor
                    processor = processor.match(logEntry)

                    val newMatches =
                        processor.matcher.completedMatches - previousProcessor.matcher.completedMatches
                    if (newMatches.isNotEmpty()) {
                        matchCount += newMatches.size

                        newMatches.forEach { match ->
                            val patternMatchEvent = PatternMatchLogEvent.from(match, logEntry.timestamp)
                            val inputMessage = logEntry.message
                            val outputMessage =
                                inputMessage.copy(
                                    event = patternMatchEvent,
                                    masID = inputMessage.masID,
                                )

                            logger.info { ObjectMessage(outputMessage) }

                            if (verbose) {
                                println("Sent pattern match to output: $patternMatchEvent")
                            }
                        }
                    }
                }

                // Periodic status update
                if (lineCount % 1000 == 0) {
                    println("Processed $lineCount lines, found $matchCount matches, errors: $errorCount")
                }
            }
        }
    }

    companion object {
        const val DEFAULT_INPUT_HOST = "0.0.0.0"
        const val DEFAULT_INPUT_PORT = 5046
        const val DEFAULT_OUTPUT_HOST = "0.0.0.0"
        const val DEFAULT_OUTPUT_PORT = 5045
    }
}

fun main(args: Array<String>) =
    LogEventsStreamProcessor()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
