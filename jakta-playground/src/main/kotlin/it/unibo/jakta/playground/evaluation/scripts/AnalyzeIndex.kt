package it.unibo.jakta.playground.evaluation.scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.jillesvangurp.ktsearch.parseHit
import com.jillesvangurp.ktsearch.searchAfter
import com.jillesvangurp.searchdsls.querydsl.matchAll
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger.Companion.extractHostnameAndPort
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.PatternMatchLogEvent
import it.unibo.jakta.playground.evaluation.KTSearch
import it.unibo.jakta.playground.evaluation.KTSearch.version
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.koin.ksp.generated.module

class AnalyzeIndex : CliktCommand() {
    private val masId: String? by option()

    private val agentId: String? by option()

    private val pgpId: String? by option()

    private val url: String by option()
        .default("http://localhost:9200")

    private val implementationLevel: Boolean by option()
        .flag()

    private val domainLevel: Boolean by option()
        .flag()

    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)
    }

    override fun run() {
        runBlocking {
            val (host, port) = extractHostnameAndPort(url)
            val client = KTSearch.createClient(host, port ?: 9200)
            println("Querying at $url (version: ${client.version()})")

            val indexName =
                when {
                    masId == null -> "jakta-default"
                    agentId == null -> "jakta-mas-$masId"
                    pgpId == null -> "jakta-mas-$masId-agent-$agentId"
                    else -> "jakta-mas-$masId-agent-$agentId-pgp-$pgpId"
                }

            println("Using index: $indexName")

            val (resp, hitsFlow) =
                client.searchAfter(indexName) {
                    resultSize = 500
                    query = matchAll()
                }

            println("reported result set size ${resp.hits?.total?.value}")

            val res = mutableListOf<LogEntry>()
            hitsFlow
                .mapNotNull { hit ->
                    val doc = hit.parseHit<LogEntry>(JaktaJsonComponent.json)
                    val logEvent = doc.message.event
                    when {
                        implementationLevel && logEvent !is PatternMatchLogEvent -> doc
                        domainLevel && logEvent is PatternMatchLogEvent -> doc
                        else -> null
                    }
                }.toCollection(res)

            if (implementationLevel) {
                res
                    .sortedBy { it.timestamp }
                    .forEach { logEvent ->
                        println("${logEvent.timestamp} : ${logEvent.message.event.description}")
                    }
            } else {
                res
                    .map { it.message.event }
                    .filterIsInstance<PatternMatchLogEvent>()
                    .sortedBy { it.originalTimestamp }
                    .forEach { logEvent ->
                        println("${logEvent.originalTimestamp} : ${logEvent.description}")
                    }
            }
        }
    }
}

fun main(args: Array<String>) =
    AnalyzeIndex()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
