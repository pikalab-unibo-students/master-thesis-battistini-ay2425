package it.unibo.jakta.agents.bdi.narrativegenerator

class StopWatch(
    var startTime: Long = 0L,
    var isRunning: Boolean = false,
) {
    private var elapsedTime: Long = 0L

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun stop() {
        elapsedTime += System.currentTimeMillis() - startTime
    }

    fun reset() {
        startTime = 0L
        elapsedTime = 0L
        isRunning = false
    }

    fun getElapsedTime(): Long = elapsedTime + (System.currentTimeMillis() - startTime)
}
