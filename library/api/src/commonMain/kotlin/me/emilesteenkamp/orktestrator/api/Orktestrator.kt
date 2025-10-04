package me.emilesteenkamp.orktestrator.api

import kotlin.coroutines.cancellation.CancellationException

interface Orktestrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(
        OrktestratorException.OrchestrationException::class,
        CancellationException::class
    )
    suspend fun orchestrate(initialState: TRANSIENT_STATE): FINALISED_STATE

    companion object
}
