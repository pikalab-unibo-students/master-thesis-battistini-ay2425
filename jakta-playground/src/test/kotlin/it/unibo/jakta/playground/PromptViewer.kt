package it.unibo.jakta.playground

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.DSLExtensions.oneStepGeneration
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder
import it.unibo.jakta.playground.MockGenerationStrategy.createOneStepStrategyWithMockedAPI
import it.unibo.jakta.playground.explorer.ExplorerBot.explorerBot
import it.unibo.jakta.playground.explorer.ExplorerBot.gridWorld
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.koin.ksp.generated.module

fun main() =
    mas {
        modules = listOf(GlobalJsonModule().module)
        // In the pgp attempt .log the prompt is shown in a human-readable form
        loggingConfig = LoggingConfig(logToFile = true)
        gridWorld()
        explorerBot(strategy = createOneStepStrategyWithMockedAPI(listOf("")))

        oneStepGeneration {
            contextFilter = DefaultFilters.defaultFilter
            promptBuilder = DefaultPromptBuilder.descriptivePrompt
        }
    }.start()
