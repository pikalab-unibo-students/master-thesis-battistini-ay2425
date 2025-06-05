package it.unibo.jakta.agents.bdi.engine.logging.events

interface LogEvent {
    val eventType: EventType get() = EventType(this.javaClass.simpleName)
    val description: String?
}
