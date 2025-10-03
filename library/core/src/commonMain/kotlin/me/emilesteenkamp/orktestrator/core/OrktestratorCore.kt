package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.OrktestratorError
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

internal class OrktestratorCore<TRANSIENT_STATE, FINALISED_STATE>(
    private val graph: Graph<TRANSIENT_STATE, FINALISED_STATE>,
) : Orktestrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(OrktestratorError.RuntimeError::class)
    override suspend fun orchestrate(initialState: TRANSIENT_STATE): FINALISED_STATE = run(
        state = initialState,
        step = graph.entryPoint(),
    )

    @Throws(OrktestratorError.RuntimeError::class)
    private tailrec suspend fun <INPUT, OUTPUT> run(
        state: TRANSIENT_STATE,
        step: Step<INPUT, OUTPUT>,
    ): FINALISED_STATE {
        val engine = graph.engineFor(step) ?: throw OrktestratorError.RuntimeError.UndefinedNextStep()
        val input = with(engine) { with(CollectorScope) { collector(state) } }
        val output = engine.executor(input)

        return when (val state = engine.modifier(state, output)) {
            is State.Final -> state.unsafeCast()
            is State.Transient -> {
                val transientState = state.unsafeCast()
                val nextStep = engine.router(transientState) ?: throw OrktestratorError.RuntimeError.NoNextStepDefined()
                run(transientState, nextStep)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorError.RuntimeError.InvalidStateModificationResult::class)
    private fun State.Final.unsafeCast(): FINALISED_STATE = this as? FINALISED_STATE
        ?: throw OrktestratorError.RuntimeError.InvalidStateModificationResult()

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorError.RuntimeError.InvalidStateModificationResult::class)
    private fun State.Transient.unsafeCast(): TRANSIENT_STATE = this as? TRANSIENT_STATE
        ?: throw OrktestratorError.RuntimeError.InvalidStateModificationResult()
}

fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator.Companion.build(
    graph: Graph<TRANSIENT_STATE, FINALISED_STATE>
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                        FINALISED_STATE : State.Final
{
    return OrktestratorCore(graph)
}