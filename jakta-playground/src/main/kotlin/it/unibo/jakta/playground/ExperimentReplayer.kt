package it.unibo.jakta.playground

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.DSLExtensions.oneStepGeneration
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder
import it.unibo.jakta.playground.MockGenerationStrategy.createOneStepStrategyWithMockedAPI
import it.unibo.jakta.playground.explorer.ExplorerBot.explorerBot
import it.unibo.jakta.playground.explorer.ExplorerBot.gridWorld
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.koin.ksp.generated.module
import java.io.File
import java.util.UUID

class ExperimentReplayer : CliktCommand() {
    val logToFile: Boolean by option()
        .flag()
        .help("Whether to run an experiment and save the new execution trace as a separate experiment.")

    val expDir: String by option()
        .help("The directory from which the previously recorded LLM responses will be taken.")
        .required()

    override fun run() {
        val expName = UUID.randomUUID().toString()
        val path = "$expDir${separator}ExplorerBot${separator}chat$separator"
        val lmResponse = extractThirdMessageFromFirstLogFile(path)

        mas {
            modules = listOf(GlobalJsonModule().module)
            loggingConfig =
                LoggingConfig(
                    logToServer = true,
                    logDir = extractDirectoryPath(expDir) + separator + expName,
                    logToFile = logToFile,
                )
            gridWorld()
            explorerBot(strategy = createOneStepStrategyWithMockedAPI(listOf(lmResponse)))

            oneStepGeneration {
                contextFilter = DefaultFilters.defaultFilter
                promptBuilder = DefaultPromptBuilder.descriptivePrompt
            }
        }.start()
    }

    companion object {
        private fun extractDirectoryPath(pathString: String): String {
            val normalizedPath = pathString.replace('\\', '/')

            val pattern = """(.+)/[^/]+$""".toRegex()
            val matchResult = pattern.find(normalizedPath.trim())

            val dirPath = matchResult?.groupValues?.get(1) ?: normalizedPath

            return if (dirPath.endsWith("/")) dirPath else "$dirPath/"
        }

        private fun extractThirdMessageFromFirstLogFile(path: String): String {
            val logFilePath = findFirstLogFile(path)
            if (logFilePath == null) {
                return "No .log files found in the directory: $path"
            }
            return extractThirdMessageFromFile(logFilePath)
        }

        private fun findFirstLogFile(directoryPath: String): String? {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                return null
            }
            val logFiles = directory.listFiles { _, name -> name.endsWith(".log") }
            return logFiles?.firstOrNull()?.absolutePath
        }

        private fun extractThirdMessageFromFile(filePath: String): String {
            try {
                val fileContent = File(filePath).readText()
                val messagePattern =
                    """INFO [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12} - """
                        .toRegex()
                val messages =
                    messagePattern
                        .split(fileContent)
                        .filter { it.isNotEmpty() }

                return if (messages.size >= 4) {
                    messages[3].trim()
                } else {
                    "Message not found in the provided file."
                }
            } catch (e: Exception) {
                return "Error reading file: ${e.message}"
            }
        }
    }
}

fun main(args: Array<String>): Unit =
    ExperimentReplayer()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
