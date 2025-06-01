package it.unibo.jakta.playground.domesticrobot

import io.kotest.core.spec.style.BehaviorSpec
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.playground.domesticrobot.patterns.DBPatternProcessor.createEventProcessor
import it.unibo.jakta.playground.domesticrobot.patterns.SiftingPatterns.domesticRobotPatterns
import it.unibo.jakta.playground.evaluation.FileProcessor.processResource
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.koin.ksp.generated.module

class DomesticRobotTest : BehaviorSpec() {
    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)

        var processor = createEventProcessor(domesticRobotPatterns)
        processResource("logs/mas_execution.jsonl") { logEntry ->
            processor = processor.match(logEntry)
        }

        val completedMatches = processor.matcher.completedMatches

        Context("Beer Request and Delivery") {
            Given("the owner requests a beer") {
                When("there is beer in stock and daily limit not reached") {
                    Then("the robot fetches beer from the fridge and delivers it to the owner") {
                        completedMatches
                    }
                }
            }
        }

        Context("Daily Limit Enforcement") {
            Given("the owner has reached daily beer limit") {
                When("the owner requests another beer") {
                    Then("the robot refuses the request and explains the health department regulation") {
                    }
                }
            }
        }

        Context("Out of Stock Handling") {
            Given("the owner requests a beer") {
                When("the robot checks and finds no beer in stock") {
                    Then("the robot orders more beer from the supermarket") {
                    }
                }
            }
        }

        Context("Beer Delivery from Supermarket") {
            Given("the robot has ordered beer from the supermarket") {
                When("the supermarket processes the order") {
                    Then("the supermarket delivers the beer to the robot and updates its beer stock") {
                    }
                }
            }
        }

        Context("Owner's Time Check Behavior") {
            Given("the owner is bored") {
                When("the owner asks the robot for the current time") {
                    Then("the robot responds with the correct time") {
                    }
                }
            }
        }

        Context("Stock Reorder Threshold") {
            Given("the robot checks the beer inventory") {
                When("the stock falls below the threshold") {
                    Then("the robot orders more beer from the supermarket and receives order confirmation") {
                    }
                }
            }
        }
    }
}
