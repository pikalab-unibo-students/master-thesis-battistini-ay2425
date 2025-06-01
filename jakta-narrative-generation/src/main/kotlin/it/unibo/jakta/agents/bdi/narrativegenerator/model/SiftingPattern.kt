package it.unibo.jakta.agents.bdi.narrativegenerator.model

data class SiftingPattern(
    val name: String,
    val description: String,
    val clauses: List<PatternClause>,
) {
    override fun toString(): String {
        val eventStrings = clauses.joinToString("\n") { it.toString() }

        return """(pattern $name
${eventStrings.prependIndent("  ")}
    """
    }
}
