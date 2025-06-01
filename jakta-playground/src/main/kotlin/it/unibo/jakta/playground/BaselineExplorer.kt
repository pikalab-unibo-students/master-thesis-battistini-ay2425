package it.unibo.jakta.playground

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.playground.explorer.ExplorerBot.baselinePlans
import it.unibo.jakta.playground.explorer.ExplorerBot.explorerBot
import it.unibo.jakta.playground.explorer.ExplorerBot.gridWorld
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.koin.ksp.generated.module

fun main(): Unit =
    mas {
        modules = listOf(GlobalJsonModule().module)
        loggingConfig = LoggingConfig(logToFile = true, logToServer = true)
        gridWorld()
        explorerBot(baselinePlans())
    }.start()
