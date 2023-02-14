package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {

    val env = Environment.of()

    val start = Jacop.parseStruct("start(0, 10)")

    val alice = Agent.of(
        name = "alice",
        beliefBase = BeliefBase.of(listOf(Belief.fromSelfSource(Jacop.parseStruct("run")))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("start(S, S)"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"hello world\", S)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("start(S, M)"),
                guard = Jacop.parseStruct("S < M & N is S + 1"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"hello world\", S)")),
                    Achieve.of(Jacop.parseStruct("start(N, M)")),
                ),
            ),
        ),
    )

    val mas = Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, alice)

    mas.start()
}
