package it.unibo.jakta.playground.gridworld.serialization

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(
    "it.unibo.jakta.agents.bdi.generationstrategies",
    "it.unibo.jakta.agents.bdi.narrativegenerator",
    "it.unibo.jakta.playground.explorer",
)
class GlobalJsonModule
