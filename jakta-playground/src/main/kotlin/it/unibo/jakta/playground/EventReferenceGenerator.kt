package it.unibo.jakta.playground

import it.unibo.jakta.agents.bdi.engine.Agent
import it.unibo.jakta.agents.bdi.engine.actions.ActionSignature
import it.unibo.jakta.agents.bdi.engine.actions.InternalRequest
import it.unibo.jakta.agents.bdi.engine.actions.effects.AddData
import it.unibo.jakta.agents.bdi.engine.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.engine.actions.effects.EventChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.IntentionChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.Pause
import it.unibo.jakta.agents.bdi.engine.actions.effects.PlanChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.engine.actions.effects.RemoveAgent
import it.unibo.jakta.agents.bdi.engine.actions.effects.RemoveData
import it.unibo.jakta.agents.bdi.engine.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.engine.actions.effects.Sleep
import it.unibo.jakta.agents.bdi.engine.actions.effects.SpawnAgent
import it.unibo.jakta.agents.bdi.engine.actions.effects.Stop
import it.unibo.jakta.agents.bdi.engine.actions.effects.UpdateData
import it.unibo.jakta.agents.bdi.engine.actions.impl.AbstractInternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalSuccess
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PlanApplicabilityResult
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.intentions.Intention
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.ActionEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.BdiEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.GoalEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.JaktaLogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.MessageEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.PlanEvent
import it.unibo.jakta.agents.bdi.engine.messages.Achieve
import it.unibo.jakta.agents.bdi.engine.messages.Message
import it.unibo.jakta.agents.bdi.engine.plans.ActivationRecord
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.plans.PlanID
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializer
import it.unibo.jakta.agents.bdi.narrativegenerator.converter.EventSerializerConfig.Companion.defaultConfig
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase
import it.unibo.jakta.agents.bdi.narrativegenerator.kb.KnowledgeBase.Companion.termFormatter
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.LogEntry
import it.unibo.jakta.agents.bdi.narrativegenerator.logging.NarrativeGenerationLogger
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature
import java.io.File

fun main() {
    val config = defaultConfig
    val converter = EventSerializer.of(config)
    val logger = NarrativeGenerationLogger.of("PatternMatcher", LoggingConfig())
    val eventKB = KnowledgeBase.empty(converter, logger)

    val fakeEventId = "id"
    val markdownFile = File("events_reference.md")
    val stringBuilder = StringBuilder()

    stringBuilder.append("# Jakta Events Reference\n\n")
    stringBuilder.append(
        "This document contains a comprehensive list of all Jakta's log events and their properties.\n\n",
    )

    fun readEvent(event: JaktaLogEvent) {
        stringBuilder.append("## ${event.javaClass.simpleName}\n\n")

        val clauses = eventKB.add(LogEntry.create(event), fakeEventId).modifiedClauses.map { it.clause }

        stringBuilder.append("```prolog\n")
        clauses.forEach { clause ->
            stringBuilder.append("${clause.head?.let { termFormatter.format(it) }}.\n")
        }
        stringBuilder.append("```\n\n")
    }

    stringBuilder.append("# Feedback Events\n\n")

    val fakeActionSignature = ActionSignature(Signature("action", 2), listOf("param_a", "param_b"))
    val fakeProvidedArguments = listOf(Atom.of("param_a"), Atom.of("param_a"))
    val fakeTrigger = AchievementGoalInvocation(Struct.of("fakeAchieve"))
    val fakeGoal = Act.of(Struct.of("fakeGoal"))
    val fakeGenerateGoal = GeneratePlan.of(fakeGoal)
    val fakePlanApplicabilityResult =
        PlanApplicabilityResult(
            fakeTrigger,
            mapOf(
                Struct.of("fakeGuard") to false,
                Struct.of("fakeGuard2") to true,
            ),
            "fake error",
        )
//    val genStrat = GenerationStrategies.oneStep(LMGenerationConfig.LMGenerationConfigContainer())
    val fakePlanId = PlanID(fakeTrigger)
    val fakeGoalList = listOf(fakeGoal, fakeGenerateGoal)
    val fakePlan = Plan.of(fakePlanId, fakeGoalList)
    val fakeBelief = Belief.wrap(Struct.of("fakeBelief"))
    val fakeAdmissibleGoal = AdmissibleGoal(fakeTrigger)
    val fakeAdmissibleBelief = AdmissibleBelief.wrap(Struct.of("fakeBelief"))

    readEvent(GoalFailure.InvalidActionArityError(fakeActionSignature, fakeProvidedArguments + Atom.of("param_c")))
    readEvent(GoalFailure.ActionSubstitutionFailure(fakeActionSignature, fakeProvidedArguments))
    readEvent(GoalFailure.ActionNotFound(listOf(fakeActionSignature), "fakeAction"))
    readEvent(GoalFailure.ActionFailure(fakeActionSignature, fakeProvidedArguments))
    readEvent(GoalFailure.TestGoalFailureFeedback(Struct.of("fakeTest")))
    readEvent(GoalSuccess.GoalExecutionSuccess(fakeGoal))
    readEvent(GoalSuccess.ActionSuccess(fakeActionSignature, fakeProvidedArguments))
    readEvent(NegativeFeedback.InapplicablePlan(listOf(fakePlanApplicabilityResult, fakePlanApplicabilityResult)))
    readEvent(NegativeFeedback.PlanNotFound(fakeTrigger))
    readEvent(PGPFailure.GenericGenerationFailure("Fake error message"))
//    readEvent(PGPSuccess.GenerationRequested(genStrat, fakeGenerateGoal))
    readEvent(
        PGPSuccess.GenerationCompleted(
            fakeGenerateGoal,
            listOf(fakePlan),
            listOf(fakeAdmissibleGoal),
            listOf(fakeAdmissibleBelief),
        ),
    )

    // Agent Events
    stringBuilder.append("# Agent Events\n\n")

    val fakeAction =
        object : AbstractInternalAction("fakeName", "message") {
            override var purpose: String? = "does nothing"

            override fun action(request: InternalRequest) {
                println("Does nothing")
            }
        }
    val fakeEvent = Event.of(fakeTrigger, null)

    readEvent(ActionEvent.ActionAddition(fakeAction))
    readEvent(BdiEvent.EventSelected(fakeEvent))
    readEvent(GoalEvent.GoalAchieved(fakeGoal, fakePlanId))

    val fakeIntention = Intention.of(recordStack = listOf(ActivationRecord.of(fakeGoalList, fakePlanId)))
    readEvent(IntentionEvent.AssignPlanToNewIntention(fakeIntention))
    readEvent(IntentionEvent.AssignPlanToExistingIntention(fakeIntention))
    readEvent(IntentionEvent.IntentionGoalRun(fakeIntention))

    readEvent(
        MessageEvent.MessageReceived(Message("sender", Achieve, Struct.of("content", Var.of("test"), Atom.of("atom")))),
    )
    readEvent(PlanEvent.PlanSelected(fakePlan))

    // Agent Changes
    stringBuilder.append("# Agent Changes\n\n")

    readEvent(BeliefChange(fakeBelief, ContextUpdate.ADDITION))
    readEvent(IntentionChange(fakeIntention, ContextUpdate.REMOVAL))
    readEvent(EventChange(fakeEvent, ContextUpdate.ADDITION))
    readEvent(PlanChange(fakePlan, ContextUpdate.ADDITION))
    readEvent(Sleep(100))
    readEvent(Stop())
    readEvent(Pause())

    // Environment Changes
    stringBuilder.append("# Environment Changes\n\n")

    val fakeAgentName = "agentId"
    val fakeAgent = Agent.of()

    readEvent(SpawnAgent(fakeAgent))
    readEvent(RemoveAgent(fakeAgentName))
    readEvent(
        SendMessage(Message("sender", Achieve, Struct.of("ordered", Atom.of("beer"), Integer.of(10))), "recipient"),
    )
    readEvent(BroadcastMessage(Message("sender", Achieve, Struct.of("content"))))
    readEvent(PopMessage(fakeAgentName))
    readEvent(AddData("key", "value"))
    readEvent(RemoveData("key"))
    readEvent(UpdateData(mapOf("key" to "value")))

    markdownFile.writeText(stringBuilder.toString())
    println("Event reference has been written to ${markdownFile.absolutePath}")
}
