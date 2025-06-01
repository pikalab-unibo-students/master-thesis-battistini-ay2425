package it.unibo.jakta.playground.evaluation.scripts

import it.unibo.jakta.playground.evaluation.IndexSearch.getAgentsFromMas
import it.unibo.jakta.playground.evaluation.IndexSearch.getPlanGenProceduresFromAgent
import it.unibo.jakta.playground.evaluation.KTSearch
import it.unibo.jakta.playground.evaluation.KTSearch.retrieveIndexNames
import it.unibo.jakta.playground.evaluation.KTSearch.version

fun main() {
    val client = KTSearch.createClient()
    println("Querying ${client.version()}")

    val indexNames = client.retrieveIndexNames()
    val masOnlyIndices = indexNames // getMasTraces(indexNames)
    masOnlyIndices
        .forEach { masId ->
            val masId = masId.substringAfter("-mas-")
            println("Found mas trace with id: $masId")
            val agentIndices =
                getAgentsFromMas(
                    indexNames,
                    masId,
                )
            agentIndices.forEach { agentId ->
                val agentId = agentId.substringAfter("-agent-")
                println("Found agent trace with id: $agentId")
                val planGenProcedures =
                    getPlanGenProceduresFromAgent(
                        indexNames,
                        masId,
                        agentId,
                    )
                planGenProcedures.forEach { pgpId ->
                    val pgpId = pgpId.substringAfter("-pgp-")
                    println("Found PGP trace with id: $pgpId")
                }
            }
        }
}
