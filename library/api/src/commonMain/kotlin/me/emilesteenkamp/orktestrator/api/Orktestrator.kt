package me.emilesteenkamp.orktestrator.api

interface Orktestrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : Orktestrator.State.Transient,
              FINALISED_STATE : Orktestrator.State.Final {
    suspend fun start(initialState: TRANSIENT_STATE): FINALISED_STATE

    interface Step<INPUT, OUTPUT> {
        object None : Step<Unit, Unit>
    }

    sealed interface State {
        interface Transient : State

        interface Final : State
    }

    object CollectorScope {
        @Suppress("UNUSED")
        fun <T : Any?> T?.requireNotNull(): T = this ?: throw InvalidStateError()

        class InvalidStateError : Error()
    }

    companion object
}
