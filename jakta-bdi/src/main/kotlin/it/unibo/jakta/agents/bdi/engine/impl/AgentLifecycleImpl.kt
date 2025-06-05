package it.unibo.jakta.agents.bdi.engine.impl

import it.unibo.jakta.agents.bdi.engine.Agent
import it.unibo.jakta.agents.bdi.engine.AgentLifecycle
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.engine.actions.InternalAction
import it.unibo.jakta.agents.bdi.engine.actions.InternalRequest
import it.unibo.jakta.agents.bdi.engine.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.EventChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.IntentionChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.Pause
import it.unibo.jakta.agents.bdi.engine.actions.effects.PlanChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.engine.actions.effects.Sleep
import it.unibo.jakta.agents.bdi.engine.actions.effects.Stop
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefUpdate
import it.unibo.jakta.agents.bdi.engine.beliefs.RetrieveResult
import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate.REMOVAL
import it.unibo.jakta.agents.bdi.engine.environment.Environment
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseRemoval
import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.events.EventQueue
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionResult
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure.ActionNotFound
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure.ActionSubstitutionFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure.InvalidActionArityError
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalSuccess.GoalExecutionSuccess
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPFailure.GenericGenerationFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PositiveFeedback
import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.goals.ActExternally
import it.unibo.jakta.agents.bdi.engine.goals.ActInternally
import it.unibo.jakta.agents.bdi.engine.goals.ActionGoal
import it.unibo.jakta.agents.bdi.engine.goals.AddBelief
import it.unibo.jakta.agents.bdi.engine.goals.BeliefGoal
import it.unibo.jakta.agents.bdi.engine.goals.EmptyGoal
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.engine.goals.Spawn
import it.unibo.jakta.agents.bdi.engine.goals.Test
import it.unibo.jakta.agents.bdi.engine.goals.TrackGoalExecution
import it.unibo.jakta.agents.bdi.engine.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.engine.intentions.Intention
import it.unibo.jakta.agents.bdi.engine.intentions.IntentionPool
import it.unibo.jakta.agents.bdi.engine.logging.events.BdiEvent.EventSelected
import it.unibo.jakta.agents.bdi.engine.logging.events.GoalEvent.GoalAchieved
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent.AssignPlanToExistingIntention
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent.AssignPlanToNewIntention
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent.IntentionGoalRun
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.MessageEvent.MessageReceived
import it.unibo.jakta.agents.bdi.engine.logging.events.PlanEvent.PlanSelected
import it.unibo.jakta.agents.bdi.engine.messages.Tell
import it.unibo.jakta.agents.bdi.engine.plangeneration.manager.GenerationManager
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.plans.PlanLibrary
import it.unibo.jakta.agents.fsm.Activity

internal data class AgentLifecycleImpl(
    override var agent: Agent,
) : AgentLifecycle {
    override var cycleCount = 0L
    private var controller: Activity.Controller? = null
    private var cachedEffects = emptyList<EnvironmentChange>()
    private val isSourceIgnored = agent.generationStrategy != null
    private val generationManager = GenerationManager.of(agent.logger, agent.loggingConfig)

    private fun log(event: LogEvent) = agent.logger?.log { event }

    override fun updateBelief(
        perceptions: BeliefBase,
        beliefBase: BeliefBase,
    ): RetrieveResult =
        when (perceptions == beliefBase) {
            false -> {
                // 1. each literal l in p not currently in b is added to b
                val rrAddition = beliefBase.addAll(perceptions)

                // 2. each literal l in b no longer in p is deleted from b
                var removedBeliefs = emptyList<BeliefUpdate>()
                var rrRemoval = RetrieveResult(removedBeliefs, rrAddition.updatedBeliefBase)
                rrRemoval.updatedBeliefBase.forEach {
                    if (!perceptions.contains(it) &&
                        it.rule.head.args
                            .first() == Belief.SOURCE_PERCEPT
                    ) {
                        rrRemoval = rrRemoval.updatedBeliefBase.remove(it)
                        removedBeliefs = removedBeliefs + rrRemoval.modifiedBeliefs
                    }
                }

                RetrieveResult(
                    rrAddition.modifiedBeliefs + rrRemoval.modifiedBeliefs,
                    rrRemoval.updatedBeliefBase,
                )
            }
            else -> RetrieveResult(emptyList(), beliefBase)
        }

    override fun selectEvent(events: EventQueue) = agent.selectEvent(events)

    override fun selectRelevantPlans(
        event: Event,
        planLibrary: PlanLibrary,
    ) = planLibrary.relevantPlans(event)

    override fun isPlanApplicable(
        event: Event,
        plan: Plan,
        beliefBase: BeliefBase,
    ) = plan.isApplicable(event, beliefBase)

    override fun selectApplicablePlan(plans: Iterable<Plan>) = agent.selectApplicablePlan(plans)

    override fun assignPlanToIntention(
        event: Event,
        plan: Plan,
        intentions: IntentionPool,
    ): Intention {
        val planActivationRecord = plan.toActivationRecord()
        if (planActivationRecord.goalQueue.isEmpty()) {
            agent.logger?.error { "[PlanToIntention] A plan must have at least a goal" }
        }
        return when (event.isExternal()) {
            true ->
                Intention.of(recordStack = listOf(planActivationRecord)).also {
                    log(AssignPlanToNewIntention(it))
                }

            false ->
                event.intention!!.pop().push(planActivationRecord).also {
                    log(AssignPlanToExistingIntention(it))
                }
        }
    }

    override fun scheduleIntention(intentions: IntentionPool) = agent.scheduleIntention(intentions)

    private fun executeInternalAction(
        intention: Intention,
        action: InternalAction,
        context: AgentContext,
        goal: ActionGoal,
    ): ExecutionResult {
        var newIntention = intention.pop()
        if (action.signature.arity < goal.action.args.size) {
            val feedback = InvalidActionArityError(action.actionSignature, goal.action.args)
            return ExecutionResult(failAchievementGoal(intention, context), feedback)
        } else {
            val internalResponse =
                action.execute(
                    InternalRequest.of(this.agent, controller?.currentTime(), goal.action.args),
                )
            // Apply substitution
            return if (internalResponse.substitution.isSuccess) {
                if (newIntention.recordStack.isNotEmpty()) {
                    newIntention = newIntention.applySubstitution(internalResponse.substitution)
                }
                val newContext = applyEffects(context, internalResponse.effects)
                ExecutionResult(
                    newContext.copy(intentions = newContext.intentions.updateIntention(newIntention)),
                    internalResponse.feedback ?: GoalExecutionSuccess(goal),
                )
            } else {
                val feedback = ActionSubstitutionFailure(action.actionSignature, goal.action.args)
                ExecutionResult(failAchievementGoal(intention, context), feedback)
            }
        }
    }

    private fun executeExternalAction(
        intention: Intention,
        action: ExternalAction,
        context: AgentContext,
        environment: Environment,
        goal: ActionGoal,
    ): ExecutionResult {
        var newIntention = intention.pop()
        if (action.signature.arity < goal.action.args.size) {
            val feedback = InvalidActionArityError(action.actionSignature, goal.action.args)
            return ExecutionResult(failAchievementGoal(intention, context), feedback)
        } else {
            val externalResponse =
                action.execute(
                    ExternalRequest.of(
                        environment,
                        agent.name,
                        controller?.currentTime(),
                        goal.action.args,
                    ),
                )
            return if (externalResponse.substitution.isSuccess) {
                if (newIntention.recordStack.isNotEmpty()) {
                    newIntention = newIntention.applySubstitution(externalResponse.substitution)
                }
                ExecutionResult(
                    context.copy(intentions = context.intentions.updateIntention(newIntention)),
                    externalResponse.feedback ?: GoalExecutionSuccess(goal),
                    externalResponse.effects,
                )
            } else {
                val feedback = ActionSubstitutionFailure(action.actionSignature, goal.action.args)
                ExecutionResult(failAchievementGoal(intention, context), feedback)
            }
        }
    }

    override fun runIntention(
        intention: Intention,
        context: AgentContext,
        environment: Environment,
    ): ExecutionResult =
        when (val nextGoal = intention.nextGoal()) {
            is EmptyGoal -> {
                ExecutionResult(
                    context.copy(intentions = context.intentions.updateIntention(intention.pop())),
                    GoalExecutionSuccess(nextGoal),
                )
            }

            is ActionGoal ->
                when (nextGoal) {
                    is ActInternally -> {
                        val internalAction = context.internalActions[nextGoal.action.functor]

                        if (internalAction == null) {
                            val feedback =
                                ActionNotFound(
                                    context.internalActions.map { it.value.actionSignature },
                                    nextGoal.action.functor,
                                )
                            ExecutionResult(failAchievementGoal(intention, context), feedback)
                        } else {
                            // Execute Internal Action
                            executeInternalAction(intention, internalAction, context, nextGoal)
                        }
                    }

                    is ActExternally -> {
                        val externalAction = environment.externalActions[nextGoal.action.functor]
                        if (externalAction == null) {
                            val feedback =
                                ActionNotFound(
                                    environment.externalActions.map { it.value.actionSignature },
                                    nextGoal.action.functor,
                                )
                            ExecutionResult(failAchievementGoal(intention, context), feedback)
                        } else {
                            // Execute External Action
                            executeExternalAction(intention, externalAction, context, environment, nextGoal)
                        }
                    }

                    is Act -> {
                        val availableActions = (environment.externalActions + context.internalActions)
                        val action = availableActions[nextGoal.action.functor]
                        if (action == null) {
                            val feedback =
                                ActionNotFound(
                                    availableActions.map { it.value.actionSignature },
                                    nextGoal.action.functor,
                                )
                            ExecutionResult(failAchievementGoal(intention, context), feedback)
                        } else {
                            // Execute Action
                            when (action) {
                                is InternalAction -> executeInternalAction(intention, action, context, nextGoal)
                                is ExternalAction ->
                                    executeExternalAction(intention, action, context, environment, nextGoal)
                                else ->
                                    throw IllegalStateException(
                                        "The Action to execute is neither internal nor external",
                                    )
                            }
                        }
                    }
                }

            is Spawn -> {
                val newIntention = intention.pop()
                ExecutionResult(
                    context.copy(
                        events = context.events + Event.ofAchievementGoalInvocation(Achieve.of(nextGoal.value)),
                        intentions = context.intentions.updateIntention(newIntention),
                    ),
                    GoalExecutionSuccess(nextGoal),
                ).also {
                    log(GoalAchieved(nextGoal, intention.currentPlan()))
                }
            }

            is Achieve -> {
                val newEvent = Event.ofAchievementGoalInvocation(nextGoal, intention)
                ExecutionResult(
                    context.copy(
                        intentions = IntentionPool.of(context.intentions - intention.id),
                        events = context.events + newEvent,
                    ),
                    GoalExecutionSuccess(nextGoal),
                ).also {
                    log(EventChange(newEvent, ADDITION))
                }
            }

            is Test -> {
                val solution = context.beliefBase.solve(nextGoal.value, isSourceIgnored)
                when (solution.isYes) {
                    true -> {
                        val newIntention = intention.pop().applySubstitution(solution.substitution)
                        ExecutionResult(
                            context.copy(intentions = context.intentions.updateIntention(newIntention)),
                            GoalExecutionSuccess(nextGoal),
                        ).also {
                            log(IntentionChange(newIntention, ADDITION))
                        }
                    }

                    else -> {
                        val failedGoal = intention.currentPlan().trigger
                        val newEvent = Event.ofTestGoalFailure(failedGoal.value, intention)
                        ExecutionResult(
                            context.copy(events = context.events + newEvent),
                            GoalFailure.TestGoalFailureFeedback(nextGoal.value),
                        ).also {
                            log(EventChange(newEvent, ADDITION))
                        }
                    }
                }
            }

            is BeliefGoal -> {
                val retrieveResult =
                    when (nextGoal) {
                        is AddBelief -> context.beliefBase.add(Belief.from(nextGoal.value))
                        is RemoveBelief -> context.beliefBase.remove(Belief.from(nextGoal.value))
                        is UpdateBelief -> context.beliefBase.update(Belief.from(nextGoal.value))
                    }
                val newIntention = intention.pop()
                ExecutionResult(
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                        intentions = context.intentions.updateIntention(newIntention),
                    ),
                    GoalExecutionSuccess(nextGoal),
                ).also {
                    log(GoalAchieved(nextGoal, intention.currentPlan()))
                }
            }

            is GeneratePlan -> {
                agent.generationStrategy?.let { strat ->
                    generationManager
                        .planGenerationStrategy
                        .generatePlan(
                            nextGoal,
                            intention,
                            strat,
                            agent.context,
                            environment,
                        )
                } ?: ExecutionResult(
                    context.copy(intentions = context.intentions.updateIntention(intention.pop())),
                    feedback = GenericGenerationFailure("Cannot generate new plans without a generation strategy"),
                )
            }

            is TrackGoalExecution -> {
                generationManager
                    .goalTrackingStrategy
                    .trackGoalExecution(
                        nextGoal,
                        intention,
                        agent.context,
                        environment,
                        ::runIntention,
                    )
            }
        }

    private fun applyEffects(
        context: AgentContext,
        effects: Iterable<AgentChange>,
    ): AgentContext {
        var newBeliefBase = context.beliefBase
        var newEvents = context.events
        var newPlans = context.planLibrary
        var newIntentions = context.intentions
        effects.forEach {
            log(it)
            when (it) {
                is BeliefChange -> {
                    val rr =
                        when (it.changeType) {
                            ADDITION -> newBeliefBase.add(it.belief)
                            REMOVAL -> newBeliefBase.remove(it.belief)
                        }
                    newBeliefBase = rr.updatedBeliefBase
                    newEvents = generateEvents(newEvents, rr.modifiedBeliefs)
                }
                is IntentionChange ->
                    newIntentions =
                        when (it.changeType) {
                            ADDITION -> newIntentions.updateIntention(it.intention)
                            REMOVAL -> newIntentions.deleteIntention(it.intention.id)
                        }
                is EventChange ->
                    newEvents =
                        when (it.changeType) {
                            ADDITION -> newEvents + it.event
                            REMOVAL -> newEvents - it.event
                        }
                is PlanChange ->
                    newPlans =
                        when (it.changeType) {
                            ADDITION -> newPlans.addPlan(it.plan)
                            REMOVAL -> newPlans.removePlan(it.plan)
                        }

                is Pause -> controller?.pause()
                is Sleep -> controller?.sleep(it.millis)
                is Stop -> controller?.stop()
            }
        }
        return context.copy(
            beliefBase = newBeliefBase,
            events = newEvents,
            planLibrary = newPlans,
            intentions = newIntentions,
        )
    }

    private fun failAchievementGoal(
        intention: Intention,
        context: AgentContext,
    ): AgentContext {
        val failedGoal = intention.currentPlan()
        val newEvent = Event.ofAchievementGoalFailure(failedGoal.trigger.value, intention)
        return context
            .copy(
                events = context.events + newEvent,
            ).also {
                log(EventChange(newEvent, ADDITION))
            }
    }

    private fun generateEvents(
        events: EventQueue,
        modifiedBeliefs: List<BeliefUpdate>,
    ): EventQueue =
        events +
            modifiedBeliefs.map {
                when (it.updateType) {
                    REMOVAL -> Event.of(BeliefBaseRemoval(it.belief))
                    ADDITION -> Event.of(BeliefBaseAddition(it.belief))
                }.also { ev ->
                    log(EventChange(ev, it.updateType))
                }
            }

    override fun sense(environment: Environment) {
        // STEP1: Perceive the Environment
        val perceptions = environment.percept()

        // STEP2: Update the BeliefBase
        val rr = updateBelief(perceptions, agent.context.beliefBase)
        rr.modifiedBeliefs.forEach {
            log(BeliefChange(it.belief, it.updateType))
        }

        var newBeliefBase = rr.updatedBeliefBase
        agent.logger?.trace { "pre-run -> ${agent.context}" }
        // Generate events related to BeliefBase revision
        var newEvents = generateEvents(agent.context.events, rr.modifiedBeliefs)

        // STEP3: Receiving Communication from Other Agents
        val message = environment.getNextMessage(agent.name)

        // STEP4: Selecting "Socially Acceptable" Messages //TODO()

        // Parse message
        if (message != null) {
            log(MessageReceived(message = message))
            newEvents =
                when (message.type) {
                    is it.unibo.jakta.agents.bdi.engine.messages.Achieve -> {
                        val goalToAchieve = Achieve.of(message.value)
                        val newEvent = Event.ofAchievementGoalInvocation(goalToAchieve)
                        newEvents +
                            newEvent.also {
                                log(EventChange(newEvent, ADDITION))
                            }
                    }
                    is Tell -> {
                        val beliefFromMessage = Belief.fromMessageSource(message.from, message.value)
                        val retrieveResult = newBeliefBase.add(beliefFromMessage)
                        newBeliefBase = retrieveResult.updatedBeliefBase
                        generateEvents(newEvents, retrieveResult.modifiedBeliefs).also {
                            log(BeliefChange(beliefFromMessage, ADDITION))
                        }
                    }
                }
            cachedEffects = cachedEffects + PopMessage(this.agent.name)
        }
        this.agent = this.agent.copy(beliefBase = newBeliefBase, events = newEvents)
    }

    override fun deliberate(environment: Environment) {
        // STEP5: Selecting an Event.
        var newPlanLibrary = this.agent.context.planLibrary
        var newEvents = this.agent.context.events
        var newIntentionPool = this.agent.context.intentions
        var newBeliefBase = this.agent.context.beliefBase
        val selectedEvent = selectEvent(this.agent.context.events)

        if (selectedEvent != null) {
            log(EventSelected(selectedEvent))

            // STEP6: Retrieving all Relevant Plans.
            val relevantPlans = selectRelevantPlans(selectedEvent, agent.context.planLibrary)

            // STEP7: Determining the Applicable Plans.
            val applicablePlans =
                relevantPlans.plans.filter {
                    isPlanApplicable(selectedEvent, it, this.agent.context.beliefBase)
                }

            // STEP8: Selecting one Applicable Plan.
            val selectedPlan = selectApplicablePlan(applicablePlans)

            if (selectedPlan != null) {
                val updatedIntention =
                    assignPlanToIntention(
                        selectedEvent,
                        selectedPlan.applicablePlan(selectedEvent, this.agent.context.beliefBase),
                        agent.context.intentions,
                    )
                newIntentionPool =
                    agent.context.intentions.updateIntention(updatedIntention).also {
                        log(PlanSelected(selectedPlan))
                    }
            } else {
                // Invalidate partial plans and/or start a new generation process.
                val result =
                    generationManager.unavailablePlanStrategy
                        .handleUnavailablePlans(
                            selectedEvent,
                            relevantPlans.plans,
                            applicablePlans.isEmpty(),
                            agent.context,
                            agent.generationStrategy,
                        ).also { r -> r.feedback?.let { log(it) } }

                val newContext = result.newAgentContext

                newEvents = newContext.events
                newIntentionPool = newContext.intentions
                newPlanLibrary = newContext.planLibrary
                newBeliefBase = newContext.beliefBase

                if (selectedEvent.isInternal() && result.feedback !is PGPSuccess.GenerationRequested) {
                    newIntentionPool =
                        newIntentionPool.deleteIntention(selectedEvent.intention!!.id).also {
                            log(IntentionChange(selectedEvent.intention!!, REMOVAL))
                        }
                }
            }

            newEvents = newEvents - selectedEvent
        }

        // Select intention to execute
        this.agent =
            this.agent.copy(
                beliefBase = newBeliefBase,
                events = newEvents,
                planLibrary = newPlanLibrary,
                intentions = newIntentionPool,
            )
    }

    override fun act(environment: Environment): Iterable<EnvironmentChange> {
        var executionResult = ExecutionResult(AgentContext.of())
        var newIntentionPool = agent.context.intentions
        if (!newIntentionPool.isEmpty()) {
            // STEP9: Select an Intention for Further Execution.
            val result = scheduleIntention(newIntentionPool)
            val scheduledIntention = result.intentionToExecute
            newIntentionPool = result.newIntentionPool

            // STEP10: Executing one Step on an Intention
            this.agent =
                if (scheduledIntention.recordStack.isEmpty()) {
                    log(IntentionChange(scheduledIntention, REMOVAL))
                    this.agent.copy(intentions = newIntentionPool)
                } else {
                    log(IntentionGoalRun(scheduledIntention))

                    executionResult =
                        runIntention(
                            scheduledIntention,
                            this.agent.context.copy(intentions = newIntentionPool),
                            environment,
                        ).also { r -> r.feedback?.let { log(it) } }

                    val context = executionResult.newAgentContext
                    val currentState = context.generationProcesses.nextGenerationState()
                    executionResult =
                        if (currentState != null) {
                            if (executionResult.feedback is PositiveFeedback) {
                                if (executionResult.feedback is PGPSuccess.GenerationCompleted) {
                                    val generationProcesses =
                                        context
                                            .generationProcesses
                                            .deleteGenerationProcess(currentState.goal)
                                    executionResult.copy(
                                        newAgentContext =
                                            context.copy(
                                                generationProcesses = generationProcesses,
                                            ),
                                    )
                                } else {
                                    executionResult
                                }
                            } else {
                                executionResult
                                    .copy(
                                        newAgentContext =
                                            context.copy(
                                                intentions = context.intentions.deleteIntention(scheduledIntention.id),
                                            ),
                                    ).also {
                                        log(IntentionChange(scheduledIntention, REMOVAL))
                                    }
                            }
                        } else {
                            executionResult
                        }

                    this.agent.copy(executionResult.newAgentContext)
                }
        }

        // Generate Environment Changes
        val environmentChangesToApply = executionResult.environmentEffects + cachedEffects
        cachedEffects = emptyList()
        return environmentChangesToApply
    }

    override fun runOneCycle(
        environment: Environment,
        controller: Activity.Controller?,
    ): Iterable<EnvironmentChange> {
        cycleCount++
        this.controller = controller
        sense(environment)
        deliberate(environment)
        return act(environment)
    }
}
