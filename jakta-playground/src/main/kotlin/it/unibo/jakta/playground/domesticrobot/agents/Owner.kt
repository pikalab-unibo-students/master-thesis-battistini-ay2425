package it.unibo.jakta.playground.domesticrobot.agents

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.plans.PlanMetadata.meaning
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Limit
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Time
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.achieve
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.beer
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.owner
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.robot
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.tell
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.print
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.random
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.send
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.sip
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.sleep
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.stop
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Substitution
import kotlin.random.Random

object Owner {
    fun MasScope.ownerAgent() =
        agent(owner.value) {
            goals {
                +achieve("get"(beer))
                +achieve("check_bored")
            }
            plans {
                +achieve("get"(beer)) then {
                    send(robot, achieve, "has"(owner, beer))
                } meaning {
                    "ask the robot to give me a beer when I want to get one."
                }

                +"has"(owner, beer).fromPercept then {
                    achieve("drink"(beer))
                } meaning {
                    "if there is beer, drink it."
                }

                -"has"(owner, beer).fromPercept then {
                    achieve("get"(beer))
                } meaning {
                    "if there is no more beer, get one."
                }

                +achieve("drink"(beer)) onlyIf {
                    "has"(owner, beer).fromPercept
                } then {
                    sip(beer)
                    achieve("drink"(beer))
                } meaning {
                    "while there is beer, sip"
                }

                +achieve("drink"(beer)) onlyIf {
                    not("has"(owner, beer).fromPercept)
                } meaning {
                    "as soon as there is no more beer, stop drinking."
                }

                +achieve("check_bored") then {
                    random(Time)
                    sleep(Time)
                    send(robot, tell, "askTime")
                    achieve("check_bored")
                } meaning {
                    "I get bored at random times. When I am bored, I ask the robot about the time."
                }

                +"time"(Time).source("robot") then {
                    print("The time is", Time)
                }

                +"msg"(C, Limit).source("robot") then {
                    print(C, Limit)
                    stop()
                }
            }
            actions {
                action("random", "number") {
                    val random = Numeric.of(Random.nextInt(100, 200))
                    val output = arguments[0].asVar()
                    output?.let { addResults(Substitution.unifier(it to random)) }
                }
            }
        }
}
