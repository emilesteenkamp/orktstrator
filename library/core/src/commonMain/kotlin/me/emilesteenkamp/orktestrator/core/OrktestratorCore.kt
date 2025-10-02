package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.Orktestrator

class OrktestratorCore<TRANSIENT_STATE, FINALISED_STATE>(
    private val graph: Graph<TRANSIENT_STATE, FINALISED_STATE>,
) : Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : Orktestrator.State.Transient,
                                                         FINALISED_STATE : Orktestrator.State.Final {
    override suspend fun start(initialState: TRANSIENT_STATE): FINALISED_STATE {
        val step = graph.initialStep() ?: error("No steps defined.")
        return run(
            state = initialState,
            step = step,
        )
    }

    private suspend fun run(
        state: TRANSIENT_STATE,
        step: Orktestrator.Step<*, *>,
    ): FINALISED_STATE {
        val runner = graph.runnerFor(step) ?: error("No step runner for step $step")

        val input =
            try {
                with(runner) {
                    with(Orktestrator.CollectorScope) {
                        collector(state)
                    }
                }
            } catch (_: Orktestrator.CollectorScope.InvalidStateError) {
                error("State in invalid state.")
            }

        val output = runner.executor(input)

        return when (val state = runner.modifier(state, output)) {
            is Orktestrator.State.Final -> state.unsafeCast()
            is Orktestrator.State.Transient -> {
                val transientState = state.unsafeCast()
                val nextStep = runner.router(transientState)
                run(transientState, nextStep)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Orktestrator.State.Final.unsafeCast(): FINALISED_STATE = this as FINALISED_STATE

    @Suppress("UNCHECKED_CAST")
    private fun Orktestrator.State.Transient.unsafeCast(): TRANSIENT_STATE = this as TRANSIENT_STATE

    class Graph<TRANSIENT_STATE, FINALISED_STATE>(
        private val map: LinkedHashMap<Orktestrator.Step<*, *>, StepRunner<TRANSIENT_STATE, FINALISED_STATE, *, *>>,
    ) where TRANSIENT_STATE : Orktestrator.State.Transient,
            FINALISED_STATE : Orktestrator.State.Final {
        fun initialStep(): Orktestrator.Step<Any, Any>? =
            map.entries
                .firstOrNull()
                ?.key
                ?.unsafeCast()

        fun runnerFor(step: Orktestrator.Step<*, *>): StepRunner<TRANSIENT_STATE, FINALISED_STATE, Any, Any>? = map[step]?.unsafeCast()

        @Suppress("UNCHECKED_CAST")
        private fun Orktestrator.Step<*, *>.unsafeCast(): Orktestrator.Step<Any, Any> = this as Orktestrator.Step<Any, Any>

        @Suppress("UNCHECKED_CAST")
        private fun StepRunner<
                TRANSIENT_STATE,
                FINALISED_STATE,
                *,
                *,
                >.unsafeCast(): StepRunner<TRANSIENT_STATE, FINALISED_STATE, Any, Any> =
            this as StepRunner<TRANSIENT_STATE, FINALISED_STATE, Any, Any>
    }


    data class StepRunner<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>(
        val collector: Orktestrator.CollectorScope.(TRANSIENT_STATE) -> INPUT,
        val modifier: (TRANSIENT_STATE, OUTPUT) -> Orktestrator.State,
        val router: ((TRANSIENT_STATE) -> (Orktestrator.Step<*, *>)),
        val executor: suspend (INPUT) -> OUTPUT,
    ) where TRANSIENT_STATE : Orktestrator.State.Transient,
            FINALISED_STATE : Orktestrator.State.Final
}