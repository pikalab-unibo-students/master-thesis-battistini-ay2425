package it.unibo.jakta.playground.explorer.gridworld

import it.unibo.jakta.playground.gridworld.GridWorld

fun main() {
    val gridWorld = GridWorld()
    println(gridWorld.data.values.joinToString("\n"))
    println(gridWorld.perception.percept().joinToString("\n"))

    val updatedGridWorld =
        gridWorld
            .updateData(mapOf("directionToMove" to "north"))
            .updateData(mapOf("directionToMove" to "north"))

    println()
    println(updatedGridWorld.perception.percept().joinToString("\n"))

    val updatedGridWorld2 =
        gridWorld
            .updateData(mapOf("directionToMove" to "east"))
            .updateData(mapOf("directionToMove" to "east"))
            .updateData(mapOf("directionToMove" to "south"))
            .updateData(mapOf("directionToMove" to "south"))

    println()
    println(updatedGridWorld2.perception.percept().joinToString("\n"))
}
