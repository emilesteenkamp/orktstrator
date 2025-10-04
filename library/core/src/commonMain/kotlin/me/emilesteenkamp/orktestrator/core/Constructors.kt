package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.core.implementation.GraphImplementation
import me.emilesteenkamp.orktestrator.core.implementation.OrktestratorImplementation

fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator.Companion.from(
    graph: Graph<TRANSIENT_STATE, FINALISED_STATE>
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                        FINALISED_STATE : State.Final
{
    return OrktestratorImplementation(graph)
}

fun <TRANSIENT_STATE, FINALISED_STATE> Graph.Companion.builder(): Graph.Builder<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    return GraphImplementation.BuilderImplementation()
}

fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator<TRANSIENT_STATE, FINALISED_STATE>.withStepInterceptor(
    stepInterceptor: StepInterceptor<*, *>
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                        FINALISED_STATE : State.Final
{
    this as OrktestratorImplementation<TRANSIENT_STATE, FINALISED_STATE>
    return OrktestratorImplementation(this.graph, this.stepInterceptorList + stepInterceptor)
}
