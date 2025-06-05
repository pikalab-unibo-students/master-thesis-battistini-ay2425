package it.unibo.jakta.playground.domesticrobot.agents

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Amount
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Limit
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.NewQuantity
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.OrderId
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Place
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Quantity
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Thing
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.Time
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.achieve
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.beer
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.fridge
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.owner
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.robot
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.supermarket
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.Literals.tell
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.close
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.hand_in
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.move_towards
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.open
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.pick
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.send
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.stop
import it.unibo.jakta.playground.domesticrobot.DomesticRobotUtils.time
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Robot {
    const val MAX_BEER_ALLOWED = 5
    const val EMPTY_STOCK = 0
    const val BEERS_TO_ORDER = 3

    fun MasScope.robotAgent() =
        agent(robot.value) {
            beliefs {
                +fact { "available"(beer, fridge) }
                +fact { "limit"(beer, MAX_BEER_ALLOWED) }
                +fact { "consumed"(beer, 0) }
                +rule {
                    "too_much"(Thing) impliedBy (
                        "consumed"(Thing, Quantity).fromSelf and
                            "limit"(Thing, Limit).fromSelf and (Quantity greaterThanOrEqualsTo Limit)
                    )
                }
            }
            plans {
                +achieve("has"(owner, beer)) onlyIf {
                    "available"(beer, fridge).fromSelf and not("too_much"(beer).fromSelf) and
                        "consumed"(beer, Quantity).fromSelf and (NewQuantity `is` Quantity + 1)
                } then {
                    achieve("at"(robot, fridge))
                    open(fridge)
                    pick(beer)
                    close(fridge)
                    achieve("at"(robot, owner))
                    hand_in(beer)
                    test("has"(owner, beer).fromPercept)
                    update("consumed"(beer, NewQuantity).fromSelf)
                }

                +achieve("has"(owner, beer)) onlyIf {
                    not("available"(beer, fridge).fromSelf)
                } then {
                    send(supermarket, achieve, "order"(beer, BEERS_TO_ORDER).source("robot"))
                }

                +achieve("has"(owner, beer)) onlyIf {
                    "too_much"(beer).fromSelf and "limit"(beer, Limit).fromSelf
                } then {
                    val cnt = "The Department of Health does not allow me to give you more beers than"
                    send(owner, tell, "msg"(cnt, Limit))
                    send(supermarket, tell, "stop")
                    stop()
                }

                +achieve("at"(robot, Place)) onlyIf { "at"(robot, Place).fromPercept }

                +achieve("at"(robot, Place)) onlyIf {
                    not("at"(robot, Place).fromPercept)
                } then {
                    move_towards(Place)
                    achieve("at"(robot, Place))
                }

                +"delivered"(beer, Quantity, OrderId).source("supermarket") then {
                    add("available"(beer, fridge).fromSelf)
                    achieve("has"(owner, beer))
                }

                +"stock"(beer, 0).fromPercept onlyIf {
                    "available"(beer, fridge).fromSelf
                } then {
                    remove("available"(beer, fridge).fromSelf)
                }

                +"stock"(beer, Amount).fromPercept onlyIf {
                    (Amount greaterThan 0) and not("available"(beer, fridge).fromSelf)
                } then {
                    update("available"(beer, fridge).fromSelf)
                }

                +"askTime".source("owner") then {
                    time(Time)
                    send(owner, tell, "time"(Time))
                    remove("askTime".source("owner"))
                }
            }
            actions {
                action("time", "current timestamp") {
                    val time =
                        DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                            .withZone(ZoneOffset.UTC)
                            .format(Instant.now())
                    val output = arguments[0].asVar()
                    output?.let { addResults(Substitution.unifier(it to Atom.of(time))) }
                }
            }
        }
}
