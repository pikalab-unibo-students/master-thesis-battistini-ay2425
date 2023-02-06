package io.github.anitvam.agents.bdi.executionstrategies

import io.github.anitvam.agents.bdi.context.AgentContext
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange

class ExecutionResult(
    val newAgentContext: AgentContext,
    val environmentEffects: Iterable<EnvironmentChange> = listOf(),
)