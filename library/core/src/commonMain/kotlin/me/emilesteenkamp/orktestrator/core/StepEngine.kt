package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.OrktestratorError
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

interface StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    @Throws(OrktestratorError.RuntimeError.RequiredValueMissing::class)
    fun CollectorScope.collector(state: TRANSIENT_STATE): INPUT
    fun modifier(state: TRANSIENT_STATE, output: OUTPUT): State
    fun router(state: TRANSIENT_STATE): Step<*, *>?
    suspend fun executor(input: INPUT): OUTPUT
}