package it.unibo.jakta.playground.evaluation.scripts

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
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.StopWatch
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import it.unibo.jakta.playground.domesticrobot.patterns.PatternProcessorFactory.createEventProcessor
import it.unibo.jakta.playground.domesticrobot.patterns.SiftingPatterns.domesticRobotPatterns
import it.unibo.jakta.playground.evaluation.FileProcessor.processFile
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.apache.logging.log4j.message.ObjectMessage
import org.koin.ksp.generated.module

class FileAnalyzer : CliktCommand() {
    val matchFile: String by option()
        .help("The log file on which the pattern-matching is run.")
        .required()

    val logToFile: Boolean by option()
        .flag()
        .help("Whether to store the matches in a file.")

    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)
    }

    override fun run() {
        var eventCounter = 1

        val processorStopWatch = StopWatch()
        var processor = createEventProcessor(domesticRobotPatterns, null, processorStopWatch)
        val loggingConfig = LoggingConfig(logToFile = logToFile)
        val logger = NarrativeGenerationLogger.create("PatternMatcher", loggingConfig)
        val stopWatch = StopWatch()

        stopWatch.start()
        processFile(matchFile) { logEntry ->
            eventCounter++
            processor = processor.match(logEntry)
            val last = processor.eventMatchResults.last()
            last.newMatches.forEach { match ->
                val patternMatchEvent = PatternMatchLogEvent.from(match, logEntry.timestamp)
                // Keep masId, agentId and pgpId if given
                val msg = logEntry.message.copy(event = patternMatchEvent)
                logger.info { ObjectMessage(msg) }
            }
        }
        stopWatch.stop()

        val completedMatches = processor.matcher.completedMatches
        val timeElapsed = stopWatch.getElapsedTime()
        val timeMatchingElapsed =
            processor.eventMatchResults
                .mapNotNull { it.timeTook }
                .sum()

        println("Total pattern matches found: ${completedMatches.size}")
        println("Took ${timeMatchingElapsed / eventCounter} ms on average to process each event")
        println("Total time elapsed: $timeElapsed ms of which spent matching: $timeMatchingElapsed ms")
    }
}

fun main(args: Array<String>) =
    FileAnalyzer()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
