package me.emilesteenkamp.orktestrator.api

import kotlin.coroutines.cancellation.CancellationException

interface Orktestrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(
        OrktestratorError.RuntimeError::class,
        CancellationException::class
    )
    suspend fun start(initialState: TRANSIENT_STATE): FINALISED_STATE

    companion object
}
