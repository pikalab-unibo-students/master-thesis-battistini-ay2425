package it.unibo.jakta.playground.domesticrobot.patterns

import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.PatternClauseMetadata.meaning
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.event
import it.unibo.jakta.agents.bdi.narrativegenerator.dsl.pattern
import it.unibo.jakta.agents.bdi.narrativegenerator.model.SiftingPattern
import it.unibo.tuprolog.core.Var

object SiftingPatterns {
    val sendMessage =
        pattern("SendMessage") {
            val e1 = varOf("E1")
            val speechAct = varOf("SpeechAct")
            val sender = varOf("Sender")
            val recipient = varOf("Recipient")

            +event(e1) {
                +"type"(e1, "SendMessage")
                +"message_type"(e1, speechAct)
                +"message_from"(e1, sender)
                +"recipient"(e1, recipient)
            }.meaning {
                +"$sender sent a $speechAct message to $recipient"
            }
        }

    val requestEvent = Var.of("Request")
    val ownerAgent = Var.of("Owner")
    val robotAgent = Var.of("Robot")
    val supermarketAgent = Var.of("Supermarket")

    val messageReceived =
        event(requestEvent) {
            +"type"("MessageReceived")
            +"message_from"(ownerAgent)
            +"message_type"("achieve")
            +"message_value"("has"(ownerAgent, "beer"))
            +"agent"(robotAgent)
        }.meaning {
            "$ownerAgent requested beer from $robotAgent"
        }

    /**
     * As an owner,
     * I want to request beer from my robot,
     * So that I can receive it without leaving my seat.
     */
    val beerRequestAndDelivery =
        pattern("BeerRequestAndDelivery") {
            val beerFetchEvent = varOf("Fetch")
            val deliverEvent = varOf("Deliver")

            +messageReceived

            +event(beerFetchEvent) {
                +"type"("ActionSuccess")
                +"action_signature_name"("pick")
                +"provided_arguments"("beer")
                +"agent"(robotAgent)
            }.meaning {
                "$robotAgent took beer from the fridge"
            }

            +event(deliverEvent) {
                +"type"("BeliefAddition")
                +"belief_content"("has"(ownerAgent, "beer"))
                +"agent"(ownerAgent)
            }.meaning {
                "$robotAgent delivered beer to $ownerAgent"
            }
        }

    /**
     * As a robot following health department rules,
     * I want to enforce daily beer consumption limits,
     * So that I comply with regulations.
     */
    val dailyLimitEnforcement =
        pattern("DailyLimitEnforcement") {
            val checkEvent = varOf("Check")
            val rejectEvent = varOf("rReject")
            val limitValue = varOf("Limit")

            +messageReceived

            +event(checkEvent) {
                +"type"("BeliefAddition")
                +"belief_content"("too_much"("beer"))
                +"agent"(robotAgent)
            }.meaning {
                "$robotAgent detected daily limit reached"
            }

            +event(rejectEvent) {
                +"type"("SendMessage")
                +"message_from"(robotAgent)
                +"message_type"("tell")
                +"message_value"(
                    "msg"("The Department of Health does not allow me to give you more beers than", limitValue),
                )
                +"recipient"(ownerAgent)
            }.meaning {
                "$robotAgent informed $ownerAgent about reaching daily limit"
            }
        }

    /**
     * As a robot,
     * I want to detect when beer is out of stock and order more,
     * So that I can continue serving the owner's requests.
     */
    val outOfStockOrdering =
        pattern("OutOfStockOrdering") {
            val stockCheck = varOf("Check")
            val orderEvent = varOf("Order")

            +event(stockCheck) {
                +"type"("BeliefAddition")
                +"belief_content"("stock"("beer", 0))
                +"agent"(robotAgent)
            }.meaning {
                "$robotAgent detected beer is out of stock"
            }

            +event(orderEvent) {
                +"type"("SendMessage")
                +"message_from"(robotAgent)
                +"message_type"("achieve")
                +"message_value"("order"("beer", 5))
                +"recipient"(supermarketAgent)
            }.meaning {
                "$robotAgent ordered more beer from $supermarketAgent"
            }
        }

    /**
     * As a supermarket,
     * I want to process and deliver beer orders from robots,
     * So that they can maintain their stock.
     */
    val beerDelivery =
        pattern("BeerDelivery") {
            val orderReceived = varOf("Receive")
            val deliveryEvent = varOf("Delivery")
            val quantity = varOf("Quantity")
            val orderId = varOf("OrderId")

            +event(orderReceived) {
                +"type"("MessageReceived")
                +"message_from"(robotAgent)
                +"message_type"("achieve")
                +"message_value"("order"("beer", quantity))
                +"agent"(supermarketAgent)
            }.meaning {
                "$supermarketAgent received order for $quantity beers from $robotAgent"
            }

            +event(deliveryEvent) {
                +"type"("SendMessage")
                +"message_from"(supermarketAgent)
                +"message_type"("tell")
                +"message_value"("delivered"("beer", quantity, orderId))
                +"recipient"(robotAgent)
            }.meaning {
                "$supermarketAgent delivered $quantity beers to $robotAgent"
            }
        }

    /**
     * As an owner,
     * I want to check the time when I'm bored,
     * So that I can stay informed.
     */
    val timeCheckBehavior =
        pattern("TimeCheckBehavior") {
            val timeRequest = varOf("Request")
            val timeResponse = varOf("Response")
            val currentTime = varOf("Time")

            +event(timeRequest) {
                +"type"("SendMessage")
                +"message_from"(ownerAgent)
                +"message_type"("tell")
                +"message_value"("askTime")
                +"recipient"(robotAgent)
            }.meaning {
                "$ownerAgent asked $robotAgent for the time"
            }

            +event(timeResponse) {
                +"type"("SendMessage")
                +"message_from"(robotAgent)
                +"message_type"("tell")
                +"message_value"("time"(currentTime))
                +"recipient"(ownerAgent)
            }.meaning {
                "$robotAgent told $ownerAgent the current time: $currentTime"
            }
        }

    /**
     * As a robot,
     * I want to order more beer when the stock falls below a threshold,
     * So that we never completely run out of beer.
     */
    val stockReorderThreshold =
        pattern("StockReorderThreshold") {
            val checkEvent = varOf("Check")
            val thresholdEvent = varOf("Threshold")
            val orderEvent = varOf("Order")
            val ackEvent = varOf("Ack")
            val stock = varOf("Stock")
            val threshold = varOf("Threshold")

            +event(checkEvent) {
                +"type"("BeliefAddition")
                +"belief_content"("stock"(stock))
                +"agent"(robotAgent)
            }.meaning {
                "$robotAgent checked beer stock: $stock bottles"
            }

            +event(thresholdEvent) {
                +"type"("BeliefAddition")
                +"belief_content"("below_threshold"(stock, threshold))
                +"agent"(robotAgent)
            }.meaning {
                "$robotAgent determined stock ($stock) is below threshold ($threshold)"
            }

            +event(orderEvent) {
                +"type"("SendMessage")
                +"message_from"(robotAgent)
                +"message_value"("ordered"("beer", 10))
                +"recipient"(supermarketAgent)
            }.meaning {
                "$robotAgent ordered more beer from $supermarketAgent"
            }

            +event(ackEvent) {
                +"type"("BeliefAddition")
                +"belief_source"(supermarketAgent)
                +"belief_content"("order_received")
                +"agent"(robotAgent)
            }.meaning {
                "$supermarketAgent confirmed the order to $robotAgent"
            }
        }

    val domesticRobotPatterns: List<SiftingPattern> =
        listOf(
            beerRequestAndDelivery,
            dailyLimitEnforcement,
            outOfStockOrdering,
            beerDelivery,
            timeCheckBehavior,
            stockReorderThreshold,
        )
}
