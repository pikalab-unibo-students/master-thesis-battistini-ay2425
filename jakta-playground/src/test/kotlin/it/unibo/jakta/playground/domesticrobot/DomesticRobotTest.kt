package it.unibo.jakta.playground.domesticrobot

import io.kotest.core.spec.style.BehaviorSpec
import it.unibo.jakta.agents.bdi.engine.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.ExecutionFeedback
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JsonModule
import it.unibo.jakta.playground.domesticrobot.patterns.PatternProcessorFactory.createEventProcessor
import it.unibo.jakta.playground.domesticrobot.patterns.SiftingPatterns.thingRequestAndDelivery
import it.unibo.jakta.playground.evaluation.FileProcessor.processResource
import it.unibo.jakta.playground.gridworld.serialization.GlobalJsonModule
import org.koin.ksp.generated.module

class DomesticRobotTest : BehaviorSpec() {
    init {
        JaktaKoin.loadAdditionalModules(JsonModule().module, GlobalJsonModule().module)

        var processor = createEventProcessor(listOf(thingRequestAndDelivery), null, null)
        processResource("execution-trace.jsonl") { logEntry ->
            println("handling ${logEntry.message.event.eventType.type}")
            if (logEntry.message.event !is AgentChange &&
                logEntry.message.event !is ExecutionFeedback
            ) {
                processor = processor.match(logEntry)
            }
        }

        val completedMatches = processor.matcher.completedMatches

        /**
         * As an owner,
         * I want to request beer from my robot,
         * So that I can receive it without leaving my seat.
         */
        Context("Beer Request and Delivery") {
            Given("the owner requests a beer") {
                When("there is beer in stock and daily limit not reached") {
                    Then("the robot fetches beer from the fridge and delivers it to the owner") {
                        completedMatches
                    }
                }
            }
        }

        /**
         * As a robot following health department rules,
         * I want to enforce daily beer consumption limits,
         * So that I comply with regulations.
         */
        Context("Daily Limit Enforcement") {
            Given("the owner has reached daily beer limit") {
                When("the owner requests another beer") {
                    Then("the robot refuses the request and explains the health department regulation") {
                    }
                }
            }
        }

        /**
         * As a robot,
         * I want to detect when beer is out of stock and order more,
         * So that I can continue serving the owner's requests.
         */
        Context("Out of Stock Handling") {
            Given("the owner requests a beer") {
                When("the robot checks and finds no beer in stock") {
                    Then("the robot orders more beer from the supermarket") {
                    }
                }
            }
        }

        /**
         * As a supermarket,
         * I want to process and deliver beer orders from robots,
         * So that they can maintain their stock.
         */
        Context("Beer Delivery from Supermarket") {
            Given("the robot has ordered beer from the supermarket") {
                When("the supermarket processes the order") {
                    Then("the supermarket delivers the beer to the robot and updates its beer stock") {
                    }
                }
            }
        }

        /**
         * As an owner,
         * I want to check the time when I'm bored,
         * So that I can stay informed.
         */
        Context("Owner's Time Check Behavior") {
            Given("the owner is bored") {
                When("the owner asks the robot for the current time") {
                    Then("the robot responds with the correct time") {
                    }
                }
            }
        }

        /**
         * As a robot,
         * I want to order more beer when the stock falls below a threshold,
         * So that we never completely run out of beer.
         */
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
