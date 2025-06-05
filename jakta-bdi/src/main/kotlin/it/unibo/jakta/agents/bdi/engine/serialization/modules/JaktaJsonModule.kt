package it.unibo.jakta.agents.bdi.engine.serialization.modules

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
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.impl.AdmissibleBeliefImpl
import it.unibo.jakta.agents.bdi.engine.beliefs.impl.BeliefImpl
import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.events.impl.EventImpl
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.GoalSuccess
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPFailure
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.goals.EmptyGoal
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.goals.impl.AchieveImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.ActExternallyImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.ActImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.ActInternallyImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.AddBeliefImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.GeneratePlanImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.RemoveBeliefImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.SpawnImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.TestImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.TrackGoalExecutionImpl
import it.unibo.jakta.agents.bdi.engine.goals.impl.UpdateBeliefImpl
import it.unibo.jakta.agents.bdi.engine.intentions.Intention
import it.unibo.jakta.agents.bdi.engine.intentions.impl.IntentionImpl
import it.unibo.jakta.agents.bdi.engine.logging.events.ActionEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.BdiEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.GoalEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.IntentionEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.MessageEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.PlanEvent
import it.unibo.jakta.agents.bdi.engine.plans.ActivationRecord
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.plans.impl.ActivationRecordImpl
import it.unibo.jakta.agents.bdi.engine.plans.impl.PartialPlanImpl
import it.unibo.jakta.agents.bdi.engine.plans.impl.PlanImpl
import it.unibo.jakta.agents.bdi.engine.serialization.AtomSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.FallbackSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.RuleSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.SignatureSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.StructSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.TermSerializer
import it.unibo.jakta.agents.bdi.engine.serialization.VarSerializer
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.Single

@Single
class JaktaJsonModule : SerializersModuleProvider {
    override val modules =
        SerializersModule {
            contextual(Term::class, TermSerializer)
            contextual(Atom::class, AtomSerializer)
            contextual(Var::class, VarSerializer)
            contextual(Struct::class, StructSerializer)
            contextual(Rule::class, RuleSerializer)
            contextual(Signature::class, SignatureSerializer)
            contextual(Any::class, FallbackSerializer)

            polymorphic(LogEvent::class) {
                // Feedback event
                subclass(GoalFailure.InvalidActionArityError::class)
                subclass(GoalFailure.ActionSubstitutionFailure::class)
                subclass(GoalFailure.ActionNotFound::class)
                subclass(GoalFailure.ActionFailure::class)
                subclass(GoalFailure.TestGoalFailureFeedback::class)
                subclass(GoalSuccess.GoalExecutionSuccess::class)
                subclass(GoalSuccess.ActionSuccess::class)
                subclass(NegativeFeedback.InapplicablePlan::class)
                subclass(NegativeFeedback.PlanNotFound::class)
                subclass(PGPFailure.GenericGenerationFailure::class)
                subclass(PGPSuccess.GenerationRequested::class)
                subclass(PGPSuccess.GenerationCompleted::class)

                // Agent Event
                subclass(ActionEvent.ActionAddition::class)
                subclass(BdiEvent.EventSelected::class)
                subclass(GoalEvent.GoalAchieved::class)
                subclass(IntentionEvent.AssignPlanToNewIntention::class)
                subclass(IntentionEvent.AssignPlanToExistingIntention::class)
                subclass(IntentionEvent.IntentionGoalRun::class)
                subclass(MessageEvent.MessageReceived::class)
                subclass(PlanEvent.PlanSelected::class)

                // Agent Change
                subclass(BeliefChange::class)
                subclass(IntentionChange::class)
                subclass(EventChange::class)
                subclass(PlanChange::class)
                subclass(Sleep::class)
                subclass(Stop::class)
                subclass(Pause::class)

                // Environment Change
                subclass(SpawnAgent::class)
                subclass(RemoveAgent::class)
                subclass(SendMessage::class)
                subclass(BroadcastMessage::class)
                subclass(PopMessage::class)
                subclass(AddData::class)
                subclass(RemoveData::class)
                subclass(UpdateData::class)
            }

            polymorphic(Intention::class) {
                subclass(IntentionImpl::class)
            }

            polymorphic(ActivationRecord::class) {
                subclass(ActivationRecordImpl::class)
            }

            polymorphic(Event::class) {
                subclass(EventImpl::class)
            }

            polymorphic(Plan::class) {
                subclass(PlanImpl::class)
                subclass(PartialPlanImpl::class)
            }

            polymorphic(Belief::class) {
                subclass(BeliefImpl::class)
                subclass(AdmissibleBeliefImpl::class)
            }

            polymorphic(Goal::class) {
                subclass(AchieveImpl::class)
                subclass(ActExternallyImpl::class)
                subclass(ActImpl::class)
                subclass(ActInternallyImpl::class)
                subclass(AddBeliefImpl::class)
                subclass(EmptyGoal::class)
                subclass(GeneratePlanImpl::class)
                subclass(RemoveBeliefImpl::class)
                subclass(SpawnImpl::class)
                subclass(TestImpl::class)
                subclass(TrackGoalExecutionImpl::class)
                subclass(UpdateBeliefImpl::class)
            }
        }
}
