package it.unibo.jakta.agents.bdi.narrativegenerator

object NarrativeGenerator {
    fun String.toSnakeCase(): String = replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
}
