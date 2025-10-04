package me.emilesteenkamp.orktestrator.core.implementation

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.OrktestratorException
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step
import me.emilesteenkamp.orktestrator.core.Graph
import me.emilesteenkamp.orktestrator.core.StepInterceptor

internal class OrktestratorImplementation<TRANSIENT_STATE, FINALISED_STATE>(
    internal val graph: Graph<TRANSIENT_STATE, FINALISED_STATE>,
    internal val stepInterceptorList: List<StepInterceptor<* ,*>> = emptyList()
) : Orktestrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(OrktestratorException.OrchestrationException::class)
    override suspend fun orchestrate(initialState: TRANSIENT_STATE): FINALISED_STATE = run(
        state = initialState,
        step = graph.entryPoint(),
    )

    @Throws(OrktestratorException.OrchestrationException::class)
    private tailrec suspend fun <INPUT, OUTPUT> run(
        state: TRANSIENT_STATE,
        step: Step<INPUT, OUTPUT>,
    ): FINALISED_STATE {
        val engine = graph.engineFor(step) ?: throw OrktestratorException.OrchestrationException.UndefinedNextStep()
        val input = with(engine) { with(CollectorScope) { collector(state) } }

        stepInterceptorList.forEach { it.onInWithUnsafeCast(input) }
        val output = try {
            engine.executor(input)
        } catch (exception: Exception) {
            stepInterceptorList.forEach { it.onException(exception) }
            throw exception
        }
        stepInterceptorList.forEach { it.onOutWithUnsafeCast(input, output) }

        return when (val state = engine.modifier(state, output)) {
            is State.Final -> state.unsafeCast()
            is State.Transient -> {
                val transientState = state.unsafeCast()
                val nextStep = engine.router(transientState) ?: throw OrktestratorException.OrchestrationException.NoNextStepDefined()
                run(transientState, nextStep)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorException.OrchestrationException.InvalidStateModificationResult::class)
    private fun State.Final.unsafeCast(): FINALISED_STATE = this as? FINALISED_STATE
        ?: throw OrktestratorException.OrchestrationException.InvalidStateModificationResult()

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorException.OrchestrationException.InvalidStateModificationResult::class)
    private fun State.Transient.unsafeCast(): TRANSIENT_STATE = this as? TRANSIENT_STATE
        ?: throw OrktestratorException.OrchestrationException.InvalidStateModificationResult()

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorException.OrchestrationException.InvalidInputIntercepted::class)
    private fun <INPUT, OUTPUT> StepInterceptor<INPUT, OUTPUT>.onInWithUnsafeCast(input: Any?) =
        this.onIn(
            input = input as? INPUT ?: throw OrktestratorException.OrchestrationException.InvalidInputIntercepted()
        )

    @Suppress("UNCHECKED_CAST")
    @Throws(
        OrktestratorException.OrchestrationException.InvalidInputIntercepted::class,
        OrktestratorException.OrchestrationException.InvalidOutputIntercepted::class
    )
    private fun <INPUT, OUTPUT> StepInterceptor<INPUT, OUTPUT>.onOutWithUnsafeCast(input: Any?, output: Any?) =
        this.onOut(
            input = input as? INPUT ?: throw OrktestratorException.OrchestrationException.InvalidInputIntercepted(),
            output = output as? OUTPUT ?: throw OrktestratorException.OrchestrationException.InvalidOutputIntercepted()
        )
}
