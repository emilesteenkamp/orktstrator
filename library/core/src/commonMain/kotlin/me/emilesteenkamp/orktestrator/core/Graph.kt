package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

interface Graph<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                        FINALISED_STATE : State.Final
{
    fun entryPoint(): Step<*, *>

    fun <INPUT, OUTPUT> engineFor(step: Step<INPUT, OUTPUT>): StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>?

    companion object
}