package me.emilesteenkamp.orktstrator.core

import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step

interface OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    fun beforeStart(initialState: TRANSIENT_STATE, initialStep: Step<*, *>)

    fun beforeStep(state: TRANSIENT_STATE, step: Step<*, *>)

    fun onIn(step: Step<*, *>, input: INPUT)

    fun onOut(step: Step<*, *>, input: INPUT, output: OUTPUT)

    fun onException(step: Step<*, *>, exception: Exception)

    fun afterStep(state: State, step: Step<*, *>)

    fun afterCompletion(finalState: FINALISED_STATE)
}