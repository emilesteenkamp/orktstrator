package me.emilesteenkamp.orktstrator.api

import kotlin.coroutines.cancellation.CancellationException

interface Orktstrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(
        OrktstratorException.OrchestrationException::class,
        CancellationException::class
    )
    suspend fun orchestrate(initialState: TRANSIENT_STATE): FINALISED_STATE

    companion object
}
