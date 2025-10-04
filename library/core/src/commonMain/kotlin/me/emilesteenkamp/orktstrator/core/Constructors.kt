package me.emilesteenkamp.orktstrator.core

import me.emilesteenkamp.orktstrator.api.Orktstrator
import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.core.implementation.OrktstratorGraphImplementation
import me.emilesteenkamp.orktstrator.core.implementation.OrktstratorImplementation

fun <TRANSIENT_STATE, FINALISED_STATE> Orktstrator.Companion.from(
    orktstratorGraph: OrktstratorGraph<TRANSIENT_STATE, FINALISED_STATE>
): Orktstrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                       FINALISED_STATE : State.Final
{
    return OrktstratorImplementation(orktstratorGraph)
}

fun <TRANSIENT_STATE, FINALISED_STATE> OrktstratorGraph.Companion.builder(): OrktstratorGraph.Builder<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    return OrktstratorGraphImplementation.BuilderImplementation()
}

fun <TRANSIENT_STATE, FINALISED_STATE> Orktstrator<TRANSIENT_STATE, FINALISED_STATE>.withStepInterceptor(
    orktstratorInterceptor: OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, *, *>
): Orktstrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                       FINALISED_STATE : State.Final
{
    this as OrktstratorImplementation<TRANSIENT_STATE, FINALISED_STATE>
    return OrktstratorImplementation(this.orktstratorGraph, this.orktstratorInterceptorList + orktstratorInterceptor)
}
