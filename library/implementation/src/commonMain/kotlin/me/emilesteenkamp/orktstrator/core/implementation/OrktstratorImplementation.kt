package me.emilesteenkamp.orktstrator.core.implementation

import kotlin.coroutines.cancellation.CancellationException
import me.emilesteenkamp.orktstrator.api.CollectorScope
import me.emilesteenkamp.orktstrator.api.Orktstrator
import me.emilesteenkamp.orktstrator.api.OrktstratorException
import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step
import me.emilesteenkamp.orktstrator.core.OrktstratorGraph
import me.emilesteenkamp.orktstrator.core.OrktstratorInterceptor

internal class OrktstratorImplementation<TRANSIENT_STATE, FINALISED_STATE>(
    internal val orktstratorGraph: OrktstratorGraph<TRANSIENT_STATE, FINALISED_STATE>,
    internal val orktstratorInterceptorList: List<OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, * ,*>> = emptyList()
) : Orktstrator<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    @Throws(
        OrktstratorException.OrchestrationException::class,
        CancellationException::class
    )
    override suspend fun orchestrate(initialState: TRANSIENT_STATE): FINALISED_STATE {
        val entryPoint = orktstratorGraph.entryPoint()

        orktstratorInterceptorList.forEach { interceptor ->
            interceptor.beforeStart(initialState, entryPoint)
        }

        return run(
            state = initialState,
            step = entryPoint,
        )
    }

    @Throws(
        OrktstratorException.OrchestrationException::class,
        CancellationException::class
    )
    private tailrec suspend fun <INPUT, OUTPUT> run(
        state: TRANSIENT_STATE,
        step: Step<INPUT, OUTPUT>,
    ): FINALISED_STATE {
        orktstratorInterceptorList.forEach { interceptor -> interceptor.beforeStep(state, step) }

        val engine = orktstratorGraph.engineFor(step) ?: throw OrktstratorException.OrchestrationException.UndefinedNextStep()
        val input = with(engine) { with(CollectorScope) { collector(state) } }

        orktstratorInterceptorList.forEach { interceptor -> interceptor.onInWithUnsafeCast(step, input) }
        val output = try {
            engine.executor(input)
        } catch (exception: Exception) {
            orktstratorInterceptorList.forEach { interceptor -> interceptor.onException(step, exception) }
            throw exception
        }
        orktstratorInterceptorList.forEach { interceptor -> interceptor.onOutWithUnsafeCast(step, input, output) }

        return when (val state = engine.modifier(state, output)) {
            is State.Final -> state.unsafeCast()
                .also { finalState ->
                    orktstratorInterceptorList.forEach { interceptor -> interceptor.afterStep(finalState, step) }
                    orktstratorInterceptorList.forEach { interceptor -> interceptor.afterCompletion(finalState) }
                }
            is State.Transient -> {
                val transientState = state.unsafeCast()
                val nextStep = engine.router(transientState) ?: throw OrktstratorException.OrchestrationException.NoNextStepDefined()
                orktstratorInterceptorList.forEach { interceptor -> interceptor.afterStep(transientState, step) }
                run(transientState, nextStep)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktstratorException.OrchestrationException.InvalidStateModificationResult::class)
    private fun State.Final.unsafeCast(): FINALISED_STATE = this as? FINALISED_STATE
        ?: throw OrktstratorException.OrchestrationException.InvalidStateModificationResult()

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktstratorException.OrchestrationException.InvalidStateModificationResult::class)
    private fun State.Transient.unsafeCast(): TRANSIENT_STATE = this as? TRANSIENT_STATE
        ?: throw OrktstratorException.OrchestrationException.InvalidStateModificationResult()

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktstratorException.OrchestrationException.InvalidInputIntercepted::class)
    private fun <INPUT, OUTPUT> OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>.onInWithUnsafeCast(
        step: Step<*, *>,
        input: Any?
    ) =
        this.onIn(
            step = step,
            input = input as? INPUT ?: throw OrktstratorException.OrchestrationException.InvalidInputIntercepted()
        )

    @Suppress("UNCHECKED_CAST")
    @Throws(
        OrktstratorException.OrchestrationException.InvalidInputIntercepted::class,
        OrktstratorException.OrchestrationException.InvalidOutputIntercepted::class
    )
    private fun <INPUT, OUTPUT> OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>.onOutWithUnsafeCast(
        step: Step<*, *>,
        input: Any?,
        output: Any?
    ) =
        this.onOut(
            step = step,
            input = input as? INPUT ?: throw OrktstratorException.OrchestrationException.InvalidInputIntercepted(),
            output = output as? OUTPUT ?: throw OrktstratorException.OrchestrationException.InvalidOutputIntercepted()
        )
}
