package it.unibo.jakta.agents.bdi.narrativegenerator.logging

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import it.unibo.jakta.agents.bdi.engine.plangeneration.PgpID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    @SerialName("@timestamp")
    val timestamp: String,
    @SerialName("ecs.version")
    val ecsVersion: String,
    @SerialName("log.level")
    val logLevel: String,
    val message: LogEventContext,
    @SerialName("process.thread.name")
    val processThreadName: String,
    @SerialName("log.logger")
    val logLogger: String,
) {
    companion object {
        fun create(
            message: LogEvent,
            masID: MasID? = MasID("masID"),
            agentID: AgentID? = AgentID("agentID"),
            pgpID: PgpID? = PgpID("pgpId"),
            timestamp: String = System.currentTimeMillis().toString(),
            ecsVersion: String = "1.20.0",
            logLevel: String = "INFO",
            processThreadName: String = "main",
            logLogger: String = "jakta",
        ): LogEntry =
            LogEntry(
                timestamp,
                ecsVersion,
                logLevel,
                LogEventContext(message, masID, agentID, pgpID),
                processThreadName,
                logLogger,
            )
    }
}
