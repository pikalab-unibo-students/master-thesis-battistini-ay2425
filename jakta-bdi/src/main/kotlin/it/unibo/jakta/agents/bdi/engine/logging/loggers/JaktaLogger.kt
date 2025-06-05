package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import it.unibo.jakta.agents.bdi.engine.plangeneration.PgpID
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.layout.template.json.util.JsonWriter
import org.apache.logging.log4j.message.ObjectMessage
import java.net.URI

interface JaktaLogger {
    val logger: Logger

    fun log(event: () -> LogEvent)

    fun trace(message: () -> Any?) = logger.trace(message)

    fun debug(message: () -> Any?) = logger.debug(message)

    fun info(message: () -> Any?) = logger.info(message)

    fun warn(message: () -> Any?) = logger.warn(message)

    fun error(message: () -> Any?) = logger.error(message)

    companion object {
        fun logger(name: String): Logger = LogManager.getLogger(name)

        @JvmStatic
        fun resolveObjectMessage(message: ObjectMessage): String {
            val param = message.parameter
            return when (param) {
                is LogEventContext -> param.event.description ?: ""
                else -> param.toString()
            }
        }

        @JvmStatic
        fun resolveObjectMessage(
            json: Json,
            message: ObjectMessage,
            jsonWriter: JsonWriter,
        ) {
            val param = message.parameter

            if (param == null) {
                jsonWriter.writeNull()
                return
            }

            when (param) {
                is LogEventContext -> {
                    try {
                        val jsonString = json.encodeToString(LogEventContext.serializer(), param)
                        jsonWriter.writeRawString(jsonString)
                    } catch (_: Exception) {
                        jsonWriter.writeString(param.toString())
                    }
                }

                else -> jsonWriter.writeString(param.toString())
            }
        }

        fun extractHostnameAndPort(urlString: String): Pair<String, Int?> =
            try {
                val url = URI(urlString)
                val hostname = url.host
                val port = if (url.port == -1) null else url.port
                Pair(hostname, port)
            } catch (e: Exception) {
                println("Invalid URL: ${e.message}")
                Pair("", null)
            }

        fun Logger.implementation(
            masID: MasID,
            event: () -> LogEvent,
            agentID: AgentID? = null,
            pgpID: PgpID? = null,
        ) {
            val eventInstance by lazy(event)
            when (val e = eventInstance) {
                is IntentionEvent -> {}
                is NegativeFeedback -> this.warn { ObjectMessage(LogEventContext(e, masID, agentID, pgpID)) }
                else -> this.info { ObjectMessage(LogEventContext(e, masID, agentID, pgpID)) }
            }
        }
    }
}
