package it.unibo.jakta.playground.evaluation

import com.jillesvangurp.ktsearch.KtorRestClient
import com.jillesvangurp.ktsearch.SearchClient
import com.jillesvangurp.ktsearch.get
import kotlinx.coroutines.runBlocking

object KTSearch {
    fun parseIndicesOutput(text: String): List<IndexStatus> =
        text.trim().lines().map { line ->
            val parts = line.split(Regex("\\s+"), 10)
            if (parts.size < 10) {
                throw IllegalArgumentException("Invalid line format: $line")
            }

            IndexStatus(
                health = parts[0],
                status = parts[1],
                index = parts[2],
                uuid = parts[3],
                pri = parts[4],
                rep = parts[5],
                docsCount = parts[6],
                docsDeleted = parts[7],
                storeSize = parts[8],
                priStoreSize = parts[9],
            )
        }

    fun SearchClient.retrieveIndexNames() =
        runBlocking {
            restClient
                .get {
                    path("_cat", "indices")
                }.getOrThrow()
                .let { resp ->
                    parseIndicesOutput(resp.text).map { it.index }
                }
        }

    fun createClient(
        host: String = "localhost",
        port: Int = 9200,
    ) = SearchClient(KtorRestClient(host = host, port = port))

    fun SearchClient.version(): String =
        runBlocking {
            val engineInfo = engineInfo()
            return@runBlocking "${engineInfo.version.distribution} ${engineInfo.version.number}"
        }
}
