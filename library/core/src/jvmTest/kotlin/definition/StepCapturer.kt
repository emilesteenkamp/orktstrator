package me.emilesteenkamp.orktstrator.definition

import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step

class StepCapturer<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    private val performedStepList = mutableListOf<Step<*, *>>()

    fun appendPerformedStep(step: Step<*, *>) = performedStepList.add(step)

    fun performedStepList() = performedStepList.toList()
}

fun <TRANSIENT_STATE, FINALISED_STATE> OrktstratorDefinition<TRANSIENT_STATE, FINALISED_STATE>.capturePerformedStep(
    stepCapturer: StepCapturer<TRANSIENT_STATE, FINALISED_STATE>
) where TRANSIENT_STATE : State.Transient,
        FINALISED_STATE : State.Final
{
    intercept<Any, Any> {
        afterStep { _, step ->
            stepCapturer.appendPerformedStep(step)
        }
    }
}