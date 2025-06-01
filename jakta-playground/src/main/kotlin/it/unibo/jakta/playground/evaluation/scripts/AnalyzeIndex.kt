package it.unibo.jakta.playground.evaluation.scripts

import com.jillesvangurp.ktsearch.parseHit
import com.jillesvangurp.ktsearch.searchAfter
import com.jillesvangurp.searchdsls.querydsl.matchAll
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.playground.evaluation.KTSearch
import it.unibo.jakta.playground.evaluation.KTSearch.version
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.koin.ksp.generated.module

fun main() {
    val client = KTSearch.createClient()
    println("Querying ${client.version()}")

    val masId = "adad66c7-f74f-436a-ad80-1e30a86d40bc"
    val agentId = "2cafa016-4464-4801-9c30-125e81e5a3f5"
//    val pgpId = "da5a605c-1eb7-4d89-993a-8df537b24ac3"
    val indexName = "jakta-default" // "jakta-mas-$masId-agent-$agentId" // -pgp-$pgpId"

    JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)

    runBlocking {
        val (resp, hitsFlow) =
            client.searchAfter(indexName) {
                resultSize = 500
                query = matchAll()
            }
        println("reported result set size ${resp.hits?.total?.value}")
        println("results in the hits flow: ${hitsFlow.count()}")
        println()

        hitsFlow
            .onEach { hit ->
                val doc = hit.parseHit<LogEntry>(JaktaJsonComponent.json)
                println(doc.message.event)
//                if (doc.message.event.eventType == "GenerationCompleted") {
//                    println(doc.message.event.eventType)
//                }
//                if (doc.message.event.eventType == "GenerationRequested") {
//                    println(doc.message.event.eventType)
//                }
            }.collect()
    }
}
