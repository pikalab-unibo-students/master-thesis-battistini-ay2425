# Jakta Events Reference

This document contains a comprehensive list of all Jakta's log events and their properties.

# Feedback Events

## InvalidActionArityError

```prolog
action_signature_arity(id, 2).
action_signature_name(id, action).
action_signature_parameter_names(id, param_a).
action_signature_parameter_names(id, param_b).
action_signature_signature_arity(id, 2).
action_signature_signature_name(id, action).
action_signature_signature_vararg(id, false).
description(id, 'The arity of the action "action" is not correct: expected 2, found [param_a, param_a, param_c]').
provided_arguments(id, param_a).
provided_arguments(id, param_a).
provided_arguments(id, param_c).
type(id, 'InvalidActionArityError').
time(id, 1748868213909).
agent(id, agentID).
```

## ActionSubstitutionFailure

```prolog
action_signature_arity(id, 2).
action_signature_name(id, action).
action_signature_parameter_names(id, param_a).
action_signature_parameter_names(id, param_b).
action_signature_signature_arity(id, 2).
action_signature_signature_name(id, action).
action_signature_signature_vararg(id, false).
description(id, 'The action "action" could not be applied with the given arguments: expected 2, found [param_a, param_a]').
provided_arguments(id, param_a).
provided_arguments(id, param_a).
type(id, 'ActionSubstitutionFailure').
time(id, 1748868214186).
agent(id, agentID).
```

## ActionNotFound

```prolog
action_not_found_name(id, fakeAction).
available_actions_arity(id, 2).
available_actions_name(id, action).
available_actions_parameter_names(id, param_a).
available_actions_parameter_names(id, param_b).
available_actions_signature_arity(id, 2).
available_actions_signature_name(id, action).
available_actions_signature_vararg(id, false).
description(id, 'The action "fakeAction" could not be found among the available actions: action').
type(id, 'ActionNotFound').
time(id, 1748868214195).
agent(id, agentID).
```

## ActionFailure

```prolog
action_signature_arity(id, 2).
action_signature_name(id, action).
action_signature_parameter_names(id, param_a).
action_signature_parameter_names(id, param_b).
action_signature_signature_arity(id, 2).
action_signature_signature_name(id, action).
action_signature_signature_vararg(id, false).
description(id, 'The action "action" failed with the given arguments: [param_a, param_a]').
provided_arguments(id, param_a).
provided_arguments(id, param_a).
type(id, 'ActionFailure').
time(id, 1748868214206).
agent(id, agentID).
```

## TestGoalFailureFeedback

```prolog
description(id, 'The goal fakeTest could not be tested').
goal_tested(id, fakeTest).
type(id, 'TestGoalFailureFeedback').
time(id, 1748868214214).
agent(id, agentID).
```

## GoalExecutionSuccess

```prolog
description(id, 'The goal Act(fakeGoal) was successfully executed').
goal_executed_value(id, fakeGoal).
goal_executed_action(id, fakeGoal).
type(id, 'GoalExecutionSuccess').
time(id, 1748868214219).
agent(id, agentID).
```

## ActionSuccess

```prolog
action_signature_arity(id, 2).
action_signature_name(id, action).
action_signature_parameter_names(id, param_a).
action_signature_parameter_names(id, param_b).
action_signature_signature_arity(id, 2).
action_signature_signature_name(id, action).
action_signature_signature_vararg(id, false).
description(id, 'The action "action" was successfully executed with the given arguments: param_a, param_a').
provided_arguments(id, param_a).
provided_arguments(id, param_a).
type(id, 'ActionSuccess').
time(id, 1748868214230).
agent(id, agentID).
```

## InapplicablePlan

```prolog
description(id, 'The following plans are not applicable: achieve fakeAchieve, achieve fakeAchieve').
plans_error(id, 'fake error').
plans_guards_fake_guard(id, false).
plans_guards_fake_guard2(id, true).
plans_trigger_value(id, fakeAchieve).
plans_trigger_goal(id, fakeAchieve).
plans_error(id, 'fake error').
plans_guards_fake_guard(id, false).
plans_guards_fake_guard2(id, true).
plans_trigger_value(id, fakeAchieve).
plans_trigger_goal(id, fakeAchieve).
type(id, 'InapplicablePlan').
time(id, 1748868214236).
agent(id, agentID).
```

## PlanNotFound

```prolog
description(id, 'No plan found for trigger achieve fakeAchieve').
trigger_value(id, fakeAchieve).
trigger_goal(id, fakeAchieve).
type(id, 'PlanNotFound').
time(id, 1748868214251).
agent(id, agentID).
```

## GenericGenerationFailure

```prolog
description(id, 'Fake error message').
type(id, 'GenericGenerationFailure').
time(id, 1748868214254).
agent(id, agentID).
```

## GenerationCompleted

```prolog
admissible_beliefs_source(id, 'Source_0').
admissible_beliefs_content(id, fakeBelief).
admissible_beliefs_rule(id, fakeBelief(source(Source))).
admissible_goals_trigger_value(id, fakeAchieve).
admissible_goals_trigger_goal(id, fakeAchieve).
description(id, 'The goal generate fakeGoal was successfully generated with the following plans: achieve fakeAchieve').
goal_goal_value(id, fakeGoal).
goal_goal_action(id, fakeGoal).
goal_value(id, fakeGoal).
plans_goals_value(id, fakeGoal).
plans_goals_action(id, fakeGoal).
plans_goals_goal_value(id, fakeGoal).
plans_goals_goal_action(id, fakeGoal).
plans_goals_value(id, fakeGoal).
plans_guard(id, true).
plans_id_guard(id, true).
plans_id_trigger_value(id, fakeAchieve).
plans_id_trigger_goal(id, fakeAchieve).
plans_trigger_value(id, fakeAchieve).
plans_trigger_goal(id, fakeAchieve).
type(id, 'GenerationCompleted').
time(id, 1748868214260).
agent(id, agentID).
```

# Agent Events

## ActionAddition

```prolog
action_purpose(id, 'does nothing').
action_action_signature_arity(id, 1).
action_action_signature_name(id, fakeName).
action_action_signature_parameter_names(id, message).
action_action_signature_signature_arity(id, 1).
action_action_signature_signature_name(id, fakeName).
action_action_signature_signature_vararg(id, false).
action_signature_arity(id, 1).
action_signature_name(id, fakeName).
action_signature_vararg(id, false).
action_name(id, fakeName).
action_type(id, internal).
description(id, 'Added internal action: fakeName').
type(id, 'ActionAddition').
time(id, 1748868214285).
agent(id, agentID).
```

## EventSelected

```prolog
description(id, 'Selected external event: achieve fakeAchieve').
event_trigger_value(id, fakeAchieve).
event_trigger_goal(id, fakeAchieve).
type(id, 'EventSelected').
time(id, 1748868214322).
agent(id, agentID).
```

## GoalAchieved

```prolog
description(id, 'Achieved goal Act(fakeGoal)').
goal_value(id, fakeGoal).
goal_action(id, fakeGoal).
plan_id_guard(id, true).
plan_id_trigger_value(id, fakeAchieve).
plan_id_trigger_goal(id, fakeAchieve).
type(id, 'GoalAchieved').
time(id, 1748868214334).
agent(id, agentID).
```

## AssignPlanToNewIntention

```prolog
description(id, 'Created new intention 35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_id_id(id, '35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_is_suspended(id, false).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_goal_queue_action(id, fakeGoal).
intention_record_stack_goal_queue_goal_value(id, fakeGoal).
intention_record_stack_goal_queue_goal_action(id, fakeGoal).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_plan_guard(id, true).
intention_record_stack_plan_trigger_value(id, fakeAchieve).
intention_record_stack_plan_trigger_goal(id, fakeAchieve).
type(id, 'AssignPlanToNewIntention').
time(id, 1748868214340).
agent(id, agentID).
```

## AssignPlanToExistingIntention

```prolog
description(id, 'Updated intention 35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_id_id(id, '35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_is_suspended(id, false).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_goal_queue_action(id, fakeGoal).
intention_record_stack_goal_queue_goal_value(id, fakeGoal).
intention_record_stack_goal_queue_goal_action(id, fakeGoal).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_plan_guard(id, true).
intention_record_stack_plan_trigger_value(id, fakeAchieve).
intention_record_stack_plan_trigger_goal(id, fakeAchieve).
type(id, 'AssignPlanToExistingIntention').
time(id, 1748868214353).
agent(id, agentID).
```

## IntentionGoalRun

```prolog
description(id, 'Running goal of 35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_id_id(id, '35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_is_suspended(id, false).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_goal_queue_action(id, fakeGoal).
intention_record_stack_goal_queue_goal_value(id, fakeGoal).
intention_record_stack_goal_queue_goal_action(id, fakeGoal).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_plan_guard(id, true).
intention_record_stack_plan_trigger_value(id, fakeAchieve).
intention_record_stack_plan_trigger_goal(id, fakeAchieve).
type(id, 'IntentionGoalRun').
time(id, 1748868214357).
agent(id, agentID).
```

## MessageReceived

```prolog
description(id, 'Received message from sender: achieve content(`test_0`, atom)').
message_from(id, sender).
message_type(id, achieve).
message_value(id, content(`test`, atom)).
type(id, 'MessageReceived').
time(id, 1748868214363).
agent(id, agentID).
```

## PlanSelected

```prolog
description(id, 'Selected plan: achieve fakeAchieve').
plan_goals_value(id, fakeGoal).
plan_goals_action(id, fakeGoal).
plan_goals_goal_value(id, fakeGoal).
plan_goals_goal_action(id, fakeGoal).
plan_goals_value(id, fakeGoal).
plan_guard(id, true).
plan_id_guard(id, true).
plan_id_trigger_value(id, fakeAchieve).
plan_id_trigger_goal(id, fakeAchieve).
plan_trigger_value(id, fakeAchieve).
plan_trigger_goal(id, fakeAchieve).
type(id, 'PlanSelected').
time(id, 1748868214371).
agent(id, agentID).
```

# Agent Changes

## BeliefChange

```prolog
belief_source(id, 'Source_0').
belief_content(id, fakeBelief).
belief_rule(id, fakeBelief(source(Source))).
change_type(id, 'ADDITION').
description(id, 'Added fakeBelief from source Source_0').
type(id, 'BeliefAddition').
time(id, 1748868214379).
agent(id, agentID).
```

## IntentionChange

```prolog
change_type(id, 'REMOVAL').
description(id, 'Removed intention 35f55094-a8ba-4b93-b549-0c0fb360593c').
type(id, 'IntentionRemoval').
intention_id_id(id, '35f55094-a8ba-4b93-b549-0c0fb360593c').
intention_is_suspended(id, false).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_goal_queue_action(id, fakeGoal).
intention_record_stack_goal_queue_goal_value(id, fakeGoal).
intention_record_stack_goal_queue_goal_action(id, fakeGoal).
intention_record_stack_goal_queue_value(id, fakeGoal).
intention_record_stack_plan_guard(id, true).
intention_record_stack_plan_trigger_value(id, fakeAchieve).
intention_record_stack_plan_trigger_goal(id, fakeAchieve).
time(id, 1748868214388).
agent(id, agentID).
```

## EventChange

```prolog
change_type(id, 'ADDITION').
description(id, 'Added external event: achieve fakeAchieve').
event_trigger_value(id, fakeAchieve).
event_trigger_goal(id, fakeAchieve).
type(id, 'EventAddition').
time(id, 1748868214397).
agent(id, agentID).
```

## PlanChange

```prolog
change_type(id, 'ADDITION').
description(id, 'Added plan: achieve fakeAchieve to the plan library').
type(id, 'PlanAddition').
plan_goals_value(id, fakeGoal).
plan_goals_action(id, fakeGoal).
plan_goals_goal_value(id, fakeGoal).
plan_goals_goal_action(id, fakeGoal).
plan_goals_value(id, fakeGoal).
plan_guard(id, true).
plan_id_guard(id, true).
plan_id_trigger_value(id, fakeAchieve).
plan_id_trigger_goal(id, fakeAchieve).
plan_trigger_value(id, fakeAchieve).
plan_trigger_goal(id, fakeAchieve).
time(id, 1748868214403).
agent(id, agentID).
```

## Sleep

```prolog
description(id, 'Agent\'s controller entered sleep state for 100 milliseconds').
millis(id, 100).
type(id, 'Sleep').
time(id, 1748868214408).
agent(id, agentID).
```

## Stop

```prolog
description(id, 'Agent\'s controller entered stop state').
type(id, 'Stop').
time(id, 1748868214413).
agent(id, agentID).
```

## Pause

```prolog
description(id, 'Agent\'s controller entered pause state').
type(id, 'Pause').
time(id, 1748868214416).
agent(id, agentID).
```

# Environment Changes

## SpawnAgent

```prolog
agent_agent_id(id, 'ae5ead81-8feb-47c9-a108-d99e39d9d242').
agent_context_internal_actions_print_purpose(id, 'prints a `Message` and its `Payload`').
agent_context_internal_actions_print_action_signature_arity(id, 2).
agent_context_internal_actions_print_action_signature_name(id, print).
agent_context_internal_actions_print_action_signature_parameter_names(id, message).
agent_context_internal_actions_print_action_signature_parameter_names(id, payload).
agent_context_internal_actions_print_action_signature_signature_arity(id, 2).
agent_context_internal_actions_print_action_signature_signature_name(id, print).
agent_context_internal_actions_print_action_signature_signature_vararg(id, false).
agent_context_internal_actions_print_signature_arity(id, 2).
agent_context_internal_actions_print_signature_name(id, print).
agent_context_internal_actions_print_signature_vararg(id, false).
agent_context_internal_actions_fail_purpose(id, 'makes the agent fail its current intention').
agent_context_internal_actions_fail_action_signature_arity(id, 0).
agent_context_internal_actions_fail_action_signature_name(id, fail).
agent_context_internal_actions_fail_action_signature_signature_arity(id, 0).
agent_context_internal_actions_fail_action_signature_signature_name(id, fail).
agent_context_internal_actions_fail_action_signature_signature_vararg(id, false).
agent_context_internal_actions_fail_signature_arity(id, 0).
agent_context_internal_actions_fail_signature_name(id, fail).
agent_context_internal_actions_fail_signature_vararg(id, false).
agent_context_internal_actions_stop_purpose(id, 'stops the agent').
agent_context_internal_actions_stop_action_signature_arity(id, 0).
agent_context_internal_actions_stop_action_signature_name(id, stop).
agent_context_internal_actions_stop_action_signature_signature_arity(id, 0).
agent_context_internal_actions_stop_action_signature_signature_name(id, stop).
agent_context_internal_actions_stop_action_signature_signature_vararg(id, false).
agent_context_internal_actions_stop_signature_arity(id, 0).
agent_context_internal_actions_stop_signature_name(id, stop).
agent_context_internal_actions_stop_signature_vararg(id, false).
agent_context_internal_actions_pause_purpose(id, 'pauses the agent').
agent_context_internal_actions_pause_action_signature_arity(id, 0).
agent_context_internal_actions_pause_action_signature_name(id, pause).
agent_context_internal_actions_pause_action_signature_signature_arity(id, 0).
agent_context_internal_actions_pause_action_signature_signature_name(id, pause).
agent_context_internal_actions_pause_action_signature_signature_vararg(id, false).
agent_context_internal_actions_pause_signature_arity(id, 0).
agent_context_internal_actions_pause_signature_name(id, pause).
agent_context_internal_actions_pause_signature_vararg(id, false).
agent_context_internal_actions_sleep_purpose(id, 'makes the agent sleep for `Time` milliseconds').
agent_context_internal_actions_sleep_action_signature_arity(id, 1).
agent_context_internal_actions_sleep_action_signature_name(id, sleep).
agent_context_internal_actions_sleep_action_signature_parameter_names(id, time).
agent_context_internal_actions_sleep_action_signature_signature_arity(id, 1).
agent_context_internal_actions_sleep_action_signature_signature_name(id, sleep).
agent_context_internal_actions_sleep_action_signature_signature_vararg(id, false).
agent_context_internal_actions_sleep_signature_arity(id, 1).
agent_context_internal_actions_sleep_signature_name(id, sleep).
agent_context_internal_actions_sleep_signature_vararg(id, false).
agent_name(id, 'ae5ead81-8feb-47c9-a108-d99e39d9d242').
description(id, 'Agent ae5ead81-8feb-47c9-a108-d99e39d9d242 has been spawned in the environment').
type(id, 'SpawnAgent').
time(id, 1748868214511).
```

## RemoveAgent

```prolog
agent_name(id, agentId).
description(id, 'Agent agentId has been removed from the environment').
type(id, 'RemoveAgent').
time(id, 1748868214638).
```

## SendMessage

```prolog
description(id, 'Sent message to recipient: achieve ordered(beer, 10)').
message_from(id, sender).
message_type(id, achieve).
message_value(id, ordered(beer, 10)).
recipient(id, recipient).
type(id, 'SendMessage').
time(id, 1748868214642).
```

## BroadcastMessage

```prolog
description(id, 'Agent sender broadcast message Achieve\n\tto all agents\n\twith content: content').
message_from(id, sender).
message_type(id, achieve).
message_value(id, content).
type(id, 'BroadcastMessage').
time(id, 1748868214645).
```

## PopMessage

```prolog
agent_name(id, agentId).
description(id, 'Popped a message from the message queue of agent agentId').
type(id, 'PopMessage').
time(id, 1748868214648).
```

## AddData

```prolog
description(id, 'Key-value key=value has been added to the environment').
key(id, key).
value(id, value).
type(id, 'AddData').
time(id, 1748868214654).
```

## RemoveData

```prolog
description(id, 'Key-value key has been removed from the environment').
key(id, key).
type(id, 'RemoveData').
time(id, 1748868214657).
```

## UpdateData

```prolog
description(id, 'Environment has been updated with the new data: {key=value}').
new_data_key(id, value).
type(id, 'UpdateData').
time(id, 1748868214660).
```

