package it.unibo.jakta.playground.evaluation

import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent.json
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import java.io.BufferedReader
import java.io.File

object FileProcessor {
    private fun readResourceFile(resourcePath: String): BufferedReader? {
        val classLoader = object {}.javaClass.enclosingClass?.classLoader ?: ClassLoader.getSystemClassLoader()
        val inputStream = classLoader.getResourceAsStream(resourcePath)
        return inputStream?.let { inputStream.bufferedReader() }
    }

    private fun processLines(
        reader: BufferedReader,
        logger: NarrativeGenerationLogger? = null,
        processFunction: (LogEntry) -> Unit,
    ) {
        var lineCount = 0
        var errorCount = 0

        reader.useLines { lines ->
            lines.forEach { line ->
                lineCount++
                val logEntry =
                    try {
                        json.decodeFromString<LogEntry>(line)
                    } catch (e: Exception) {
                        errorCount++
                        logger?.warn { "Could not parse line $lineCount: ${e.message} as a LogEntry." }
                        null
                    }
                logEntry?.let { processFunction(it) }
            }
        }
        logger?.info { "Processing complete. Total entries: $lineCount, Events not parseable: $errorCount" }
    }

    /**
     * Process a JSON Lines file from the filesystem
     */
    fun processFile(
        logFilePath: String,
        logger: NarrativeGenerationLogger? = null,
        processFunction: (LogEntry) -> Unit,
    ) {
        val file = File(logFilePath)
        val reader = file.bufferedReader()
        processLines(reader, logger, processFunction)
    }

    /**
     * Process a JSON Lines file from resources
     */
    fun processResource(
        resourcePath: String,
        logger: NarrativeGenerationLogger? = null,
        processFunction: (LogEntry) -> Unit,
    ) {
        val reader = readResourceFile(resourcePath)
        reader?.let { processLines(it, logger, processFunction) }
            ?: logger?.error { "Resource not found: $resourcePath" }
    }
}
