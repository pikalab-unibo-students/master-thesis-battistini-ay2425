package it.unibo.jakta.playground.domesticrobot.patterns

import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.PatternClauseMetadata.meaning
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.event
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.pattern
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.jakta.playground.domesticrobot.agents.Robot.EMPTY_STOCK
import it.unibo.tuprolog.core.Var

@Suppress("LocalVariableName", "ktlint:standard:property-naming")
object SiftingPatterns {
    val RequestEvent = Var.of("Request")
    val Owner = Var.of("Owner")
    val Robot = Var.of("Robot")
    val Thing = Var.of("Thing")
    val Supermarket = Var.of("Supermarket")
    val Quantity = Var.of("Quantity")

    val messageReceivedHasOwnerThing =
        event(RequestEvent) {
            +"type"(RequestEvent, "MessageReceived")
            +"message_from"(RequestEvent, Owner)
            +"message_type"(RequestEvent, "achieve")
            +"message_value"(RequestEvent, "has"(Owner, Thing))
            +"agent"(RequestEvent, Robot)
        }.meaning {
            +"$Owner requested $Thing from $Robot"
        }

    val thingRequestAndDelivery =
        pattern("ThingRequestAndDelivery") {
            val FetchEvent = varOf("Fetch")
            val DeliverEvent = varOf("Deliver")

            +messageReceivedHasOwnerThing

            +event(FetchEvent) {
                +"type"(FetchEvent, "ActionSuccess")
                +"action_signature_name"(FetchEvent, "pick")
                +"provided_arguments"(FetchEvent, Thing)
                +"agent"(FetchEvent, Robot)
            }.meaning {
                +"$Robot took $Thing from the fridge"
            }

            +event(DeliverEvent) {
                +"type"(DeliverEvent, "BeliefAddition")
                +"belief_content"(DeliverEvent, "has"(Owner, Thing))
                +"agent"(DeliverEvent, Owner)
            }.meaning {
                +"$Robot delivered $Thing to $Owner"
            }

            description =
                """
                The ${eventClauses[0].purpose}, so the ${eventClauses[1].purpose} and ${eventClauses[2].purpose} 
                """.trimIndent()
        }

    val dailyLimitEnforcement =
        pattern("DailyLimitEnforcement") {
            val RejectEvent = varOf("rReject")
            val LimitValue = varOf("Limit")

            +messageReceivedHasOwnerThing

            +event(RejectEvent) {
                +"type"(RejectEvent, "SendMessage")
                +"message_from"(RejectEvent, Robot)
                +"message_type"(RejectEvent, "tell")
                +"message_value"(
                    RejectEvent,
                    "msg"("The Department of Health does not allow me to give you more beers than", LimitValue),
                )
                +"recipient"(RejectEvent, Owner)
            }.meaning {
                +"$Robot informed $Owner about reaching daily limit"
            }

            description =
                """
                Since the ${eventClauses[0].purpose} and the daily limit has been reached, the ${eventClauses[1].purpose}.
                """.trimIndent()
        }

    val outOfStockOrdering =
        pattern("OutOfStockOrdering") {
            val StockCheck = varOf("Check")
            val OrderEvent = varOf("Order")

            +event(StockCheck) {
                +"type"(StockCheck, "BeliefAddition")
                +"belief_content"(StockCheck, "stock"(Thing, EMPTY_STOCK))
                +"agent"(StockCheck, Robot)
            }.meaning {
                +"$Robot detected $Thing is out of stock"
            }

            val msgPayload = "order"(Thing, Quantity).source("robot")

            +event(OrderEvent) {
                +"type"(OrderEvent, "SendMessage")
                +"message_from"(OrderEvent, Robot)
                +"message_type"(OrderEvent, "achieve")
                +"message_value"(OrderEvent, msgPayload)
                +"recipient"(OrderEvent, Supermarket)
            }.meaning {
                +"$Robot ordered more $Thing from $Supermarket"
            }

            description =
                """
                The ${eventClauses[0].purpose} so the ${eventClauses[1].purpose}
                """.trimIndent()
        }

    val thingDelivery =
        pattern("ThingDelivery") {
            val OrderReceived = varOf("Receive")
            val DeliveryEvent = varOf("Delivery")
            val OrderId = varOf("OrderId")

            val msgPayload = "order"(Thing, Quantity).source("robot")

            +event(OrderReceived) {
                +"type"(OrderReceived, "MessageReceived")
                +"message_from"(OrderReceived, Robot)
                +"message_type"(OrderReceived, "achieve")
                +"message_value"(OrderReceived, msgPayload)
                +"agent"(OrderReceived, Supermarket)
            }.meaning {
                +"$Supermarket received order for $Quantity ${Thing}s from $Robot"
            }

            +event(DeliveryEvent) {
                +"type"(DeliveryEvent, "SendMessage")
                +"message_from"(DeliveryEvent, Supermarket)
                +"message_type"(DeliveryEvent, "tell")
                +"message_value"(DeliveryEvent, "delivered"(Thing, Quantity, OrderId))
                +"recipient"(DeliveryEvent, Robot)
            }.meaning {
                +"$Supermarket delivered $Quantity ${Thing}s to $Robot"
            }

            description =
                """
                Since the ${eventClauses[0].purpose} the ${eventClauses[1].purpose}
                """.trimIndent()
        }

    val timeCheckBehavior =
        pattern("TimeCheckBehavior") {
            val TimeRequest = varOf("Request")
            val TimeResponse = varOf("Response")
            val CurrentTime = varOf("Time")

            +event(TimeRequest) {
                +"type"(TimeRequest, "SendMessage")
                +"message_from"(TimeRequest, Owner)
                +"message_type"(TimeRequest, "tell")
                +"message_value"(TimeRequest, "askTime")
                +"recipient"(TimeRequest, Robot)
            }.meaning {
                +"$Owner asked $Robot for the time"
            }

            +event(TimeResponse) {
                +"type"(TimeResponse, "SendMessage")
                +"message_from"(TimeResponse, Robot)
                +"message_type"(TimeResponse, "tell")
                +"message_value"(TimeResponse, "time"(CurrentTime))
                +"recipient"(TimeResponse, Owner)
            }.meaning {
                +"$Robot told $Owner that the current time is: $CurrentTime"
            }

            description =
                """
                The ${eventClauses[0].purpose} so the ${eventClauses[1].purpose}
                """.trimIndent()
        }

    val domesticRobotPatterns: List<SiftingPattern> =
        listOf(
            thingRequestAndDelivery,
            dailyLimitEnforcement,
            outOfStockOrdering,
            thingDelivery,
            timeCheckBehavior,
        )
}
