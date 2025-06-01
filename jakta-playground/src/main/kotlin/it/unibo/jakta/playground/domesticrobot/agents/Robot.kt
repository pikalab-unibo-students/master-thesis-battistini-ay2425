package it.unibo.jakta.playground.domesticrobot.agents

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Amount
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Limit
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.NewQuantity
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.OrderId
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Place
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Quantity
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Thing
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Time
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.achieve
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.beer
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.fridge
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.owner
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.robot
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.supermarket
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.tell
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.close
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.hand_in
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.move_towards
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.open
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.pick
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.send
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.time
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Robot {
    fun MasScope.robotAgent() =
        agent(robot.value) {
            beliefs {
                +fact { "available"(beer, fridge) }
                +fact { "limit"(beer, 10) }
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
                    send(supermarket, achieve, "order"(beer, 5).source("robot"))
                }

                +achieve("has"(owner, beer)) onlyIf {
                    "too_much"(beer).fromSelf and "limit"(beer, Limit).fromSelf
                } then {
                    val cnt = "The Department of Health does not allow me to give you more beers than"
                    send(owner, tell, "msg"(cnt, Limit))
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
