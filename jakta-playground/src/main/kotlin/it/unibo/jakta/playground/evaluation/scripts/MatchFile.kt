package it.unibo.jakta.playground.evaluation.scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.JaktaLogEventContainer
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import it.unibo.jakta.playground.domesticrobot.patterns.DBPatternProcessor.createEventProcessor
import it.unibo.jakta.playground.domesticrobot.patterns.SiftingPatterns.sendMessage
import it.unibo.jakta.playground.evaluation.FileProcessor.processFile
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.apache.logging.log4j.message.ObjectMessage
import org.koin.ksp.generated.module

class FileAnalyzer : CliktCommand() {
    val expFile: String by option()
        .help("The log file to analyze.")
        .required()

    val reportFileName: String? by option()
        .help(
            "The name of the file where the matches will be written." +
                " If not specified, the matches will be printed to the console instead.",
        )

    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)
    }

    override fun run() {
        var processor = createEventProcessor(listOf(sendMessage))
        val logger =
            NarrativeGenerationLogger.of(
                reportFileName ?: "PatternMatcher",
                LoggingConfig(
                    logToFile = reportFileName != null,
                ),
            )

        processFile(expFile) { logEntry ->
            processor = processor.match(logEntry)

            val patternMatches = processor.matcher.completedMatches
            patternMatches.forEach { match ->
                val patternMatchEvent = PatternMatchLogEvent.from(match)
                // Keep masId, agentId and pgpId if given
                val msg: JaktaLogEventContainer = logEntry.message.copy(event = patternMatchEvent)
                logger.info { ObjectMessage(msg) }
            }
        }

        val patternMatches = processor.matcher.completedMatches
        println("Total pattern matches found: ${patternMatches.size}")
    }
}

fun main(args: Array<String>) =
    FileAnalyzer()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
