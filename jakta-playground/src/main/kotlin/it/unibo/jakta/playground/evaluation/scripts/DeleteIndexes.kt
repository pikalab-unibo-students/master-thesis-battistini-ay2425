package it.unibo.jakta.playground.evaluation.scripts

import com.jillesvangurp.ktsearch.deleteIndex
import it.unibo.jakta.playground.evaluation.IndexSearch.getMasTraces
import it.unibo.jakta.playground.evaluation.KTSearch
import it.unibo.jakta.playground.evaluation.KTSearch.retrieveIndexNames
import it.unibo.jakta.playground.evaluation.KTSearch.version

/**
 * Deletes all the indexes created by running a Mas.
 */
suspend fun main() {
    val client = KTSearch.createClient()
    println("Querying ${client.version()}")

    val indexNames = client.retrieveIndexNames()
    val masOnlyIndices = getMasTraces(indexNames)

    client.deleteIndex("jakta-default")

//    masOnlyIndices
//        .forEach { masIdx ->
//            val masId = masIdx.substringAfter("-mas-")
//            println("Deleting mas trace with id: $masId")
//            val agentIndices =
//                getAgentsFromMas(
//                    indexNames,
//                    masId,
//                )
//            client.deleteIndex(masIdx)
//            agentIndices.forEach { agentIdx ->
//                val agentId = agentIdx.substringAfter("-agent-")
//                println("Deleting agent trace with id: $agentId")
//                val planGenProcedures =
//                    getPlanGenProceduresFromAgent(
//                        indexNames,
//                        masId,
//                        agentId,
//                    )
//                client.deleteIndex(agentIdx)
//                planGenProcedures.forEach { pgpIdx ->
//                    val pgpId = pgpIdx.substringAfter("-pgp-")
//                    println("Deleting PGP trace with id: $pgpId")
//                    client.deleteIndex(pgpIdx)
//                }
//            }
//        }
}
