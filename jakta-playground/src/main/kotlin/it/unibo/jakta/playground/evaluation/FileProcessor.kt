package it.unibo.jakta.playground.evaluation

import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent.json
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
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
                        print("Error parsing line $lineCount: ${e.message}")
                        null
                    }
                logEntry?.let { processFunction(it) }
            }
        }
        println("Processing complete. Total entries: $lineCount, Errors: $errorCount")
    }

    /**
     * Process a JSON Lines file from the filesystem
     */
    fun processFile(
        logFilePath: String,
        processFunction: (LogEntry) -> Unit,
    ) {
        val file = File(logFilePath)
        val reader = file.bufferedReader()
        processLines(reader, processFunction)
    }

    /**
     * Process a JSON Lines file from resources
     */
    fun processResource(
        resourcePath: String,
        processFunction: (LogEntry) -> Unit,
    ) {
        val reader = readResourceFile(resourcePath)
        reader?.let { processLines(it, processFunction) } ?: println("Resource not found: $resourcePath")
    }
}
