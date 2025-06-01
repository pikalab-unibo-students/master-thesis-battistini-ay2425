package it.unibo.jakta.playground.domesticrobot.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ExternalActionScope
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalSuccess
import it.unibo.jakta.agents.bdi.engine.messages.Achieve
import it.unibo.jakta.agents.bdi.engine.messages.Message
import it.unibo.jakta.agents.bdi.engine.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

object HouseDsl {
    fun MasScope.houseEnvironment() =
        environment {
            from(HouseEnvironment())
            actions {
                action("open", "an openable object") { executeHouseAction() }
                action("close", "a closeable object") { executeHouseAction() }
                action("pick", "something") { executeHouseAction() }
                action("hand_in", "something") { executeHouseAction() }
                action("sip", "a drinkable object") { executeHouseAction() }
                action("move_towards", "an object") { executeHouseAction() }
                action("deliver", "an object", "in a given quantity") { executeHouseAction() }

                action("send", 3) {
                    val receiver: Atom = argument(0)
                    val type: Atom = argument(1)
                    val message: Struct = argument(2)
                    when (type.value) {
                        "tell" -> {
                            sendMessage(receiver.value, Message(this.sender, Tell, message))
                            addFeedback(GoalSuccess.ActionSuccess(actionSignature, arguments))
                        }
                        "achieve" -> {
                            sendMessage(
                                receiver.value,
                                Message(this.sender, Achieve, message),
                            )
                            addFeedback(GoalSuccess.ActionSuccess(actionSignature, arguments))
                        }
                    }
                }
            }
        }

    private fun ExternalActionScope.executeHouseAction() {
        val actionName =
            when {
                arguments.size == 1 -> {
                    val arg = arguments[0].asAtom()?.value ?: return
                    "${actionSignature.name}($arg)"
                }
                arguments.isNotEmpty() -> {
                    val args = arguments.joinToString(", ") { it.toString() }
                    "${actionSignature.name}($args)"
                }
                else -> actionSignature.name // For actions without arguments
            }

        val env = environment as? HouseEnvironment
        if (env != null) {
            val res = env.parseAction(actionName)
            res?.let { updateData("model" to res) }

            val feedback =
                if (res != null) {
                    GoalSuccess.ActionSuccess(actionSignature, arguments)
                } else {
                    GoalFailure.ActionFailure(actionSignature, arguments)
                }

            addFeedback(feedback)
        } else {
            val feedback =
                GoalFailure.ActionFailure(
                    actionSignature,
                    arguments,
                    "Only HouseEnvironment is supported",
                )
            addFeedback(feedback)
        }
    }
}
