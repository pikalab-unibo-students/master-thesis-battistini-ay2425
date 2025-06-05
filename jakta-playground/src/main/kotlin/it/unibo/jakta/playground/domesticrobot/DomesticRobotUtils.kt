package it.unibo.jakta.playground.domesticrobot

import it.unibo.jakta.agents.bdi.dsl.plans.BodyScope
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("FunctionName")
object DomesticRobotUtils {
    private fun BodyScope.executeSafely(
        command: String,
        vararg args: Any,
    ) {
        val safeArgs =
            args
                .mapNotNull {
                    when (it) {
                        is String -> Atom.of(it)
                        is Integer -> Numeric.of(it)
                        is Term -> it
                        else -> null
                    }
                }.ifEmpty { null }
        safeArgs?.let { execute(Struct.of(command, it)) } ?: execute(Struct.of(command))
    }

    // External actions
    fun BodyScope.open(vararg args: Any) = executeSafely("open", *args)

    fun BodyScope.close(vararg args: Any) = executeSafely("close", *args)

    fun BodyScope.pick(vararg args: Any) = executeSafely("pick", *args)

    fun BodyScope.hand_in(vararg args: Any) = executeSafely("hand_in", *args)

    fun BodyScope.sip(vararg args: Any) = executeSafely("sip", *args)

    fun BodyScope.move_towards(vararg args: Any) = executeSafely("move_towards", *args)

    fun BodyScope.deliver(vararg args: Any) = executeSafely("deliver", *args)

    fun BodyScope.send(vararg args: Any) = executeSafely("send", *args)

    // Internal actions
    fun BodyScope.stop(vararg args: Any) = executeSafely("stop", *args)

    fun BodyScope.print(vararg args: Any) = executeSafely("print", *args)

    fun BodyScope.sleep(vararg args: Any) = executeSafely("sleep", *args)

    fun BodyScope.random(vararg args: Any) = executeSafely("random", *args)

    fun BodyScope.time(vararg args: Any) = executeSafely("time", *args)

    private fun ownNameAsAtom() = ownNameAs { Atom.of(it) }

    private fun <T> ownNameAs(transform: (String) -> T) =
        object : ReadOnlyProperty<Any?, T> {
            override fun getValue(
                thisRef: Any?,
                property: KProperty<*>,
            ): T = transform(property.name)
        }

    object Literals {
        val Product = Var.of("Product")
        val Quantity = Var.of("Quantity")
        val NewQuantity = Var.of("NewQuantity")
        val OrderId = Var.of("OrderId")
        val Limit = Var.of("Limit")
        val Place = Var.of("Place")
        val Thing = Var.of("Thing")
        val Amount = Var.of("Amount")
        val Time = Var.of("Time")

        val achieve: Atom by ownNameAsAtom()
        val tell: Atom by ownNameAsAtom()

        val robot: Atom by ownNameAsAtom()
        val owner: Atom by ownNameAsAtom()
        val supermarket: Atom by ownNameAsAtom()
        val fridge: Atom by ownNameAsAtom()
        val beer: Atom by ownNameAsAtom()
    }
}
