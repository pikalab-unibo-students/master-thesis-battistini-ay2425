package io.github.anitvam.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec

class TestGoalsDsl : DescribeSpec({
    describe("Agent's initial goals") {
        it("can be specified using the DSL") {
            val goals = goals {
                achieve("send_ping"(E)) // Atoms are not converted to Structs from LogicProgrammingScope ???
                test("sendMessageTo"("ball", R))
            }

            println(goals)
        }
    }
})