package it.unibo.jakta.playground.domesticrobot

import it.unibo.jakta.agents.bdi.dsl.loggingConfig
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.playground.domesticrobot.agents.Owner.ownerAgent
import it.unibo.jakta.playground.domesticrobot.agents.Robot.robotAgent
import it.unibo.jakta.playground.domesticrobot.agents.Supermarket.supermarketAgent
import it.unibo.jakta.playground.domesticrobot.environment.HouseDsl.houseEnvironment
import it.unibo.jakta.playground.experiment.AbstractApplication

class DomesticRobotApplication : AbstractApplication() {
    override fun createMas(logConfig: LoggingConfig) =
        mas {
            loggingConfig = logConfig
            executionStrategy = ExecutionStrategy.oneThreadPerMas()

            houseEnvironment()
            robotAgent()
            ownerAgent()
            supermarketAgent()
        }

    override fun createLoggingConfig(expName: String) =
        loggingConfig {
            logToFile = this@DomesticRobotApplication.logToFile
            logToConsole = this@DomesticRobotApplication.logToConsole
            logToServer = this@DomesticRobotApplication.logToServer
            logToSingleFile = this@DomesticRobotApplication.logToSingleFile
            logLevel = this@DomesticRobotApplication.logLevel.level
            logDir = this@DomesticRobotApplication.logDir
            logServerUrl = this@DomesticRobotApplication.logServerUrl
        }
}
