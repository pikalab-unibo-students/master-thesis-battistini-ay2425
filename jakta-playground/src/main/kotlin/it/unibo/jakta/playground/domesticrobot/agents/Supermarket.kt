package it.unibo.jakta.playground.domesticrobot.agents

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.OrderId
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Product
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.Quantity
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.supermarket
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.Literals.tell
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.deliver
import it.unibo.jakta.playground.domesticrobot.DomesticRobotMas.send

object Supermarket {
    fun MasScope.supermarketAgent() =
        agent(supermarket.value) {
            beliefs {
                +fact { "last_order_id"(0) }
            }
            plans {
                +achieve("order"("source"(A), Product, Quantity)) onlyIf {
                    "last_order_id"(N).fromSelf and (OrderId `is` N + 1)
                } then {
                    update("last_order_id"(OrderId).fromSelf)
                    deliver(Product, Quantity)
                    send(A, tell, "delivered"(Product, Quantity, OrderId))
                }
            }
        }
}
