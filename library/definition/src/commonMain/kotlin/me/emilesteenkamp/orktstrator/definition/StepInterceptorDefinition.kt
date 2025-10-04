package me.emilesteenkamp.orktstrator.definition

import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step
import me.emilesteenkamp.orktstrator.core.OrktstratorInterceptor

class StepInterceptorDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> internal constructor()
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    var beforeStartInterceptor: ((TRANSIENT_STATE, Step<*, *>) -> Unit)? = null
    var beforeStepInterceptor: ((TRANSIENT_STATE, Step<*, *>) -> Unit)? = null
    var onInInterceptor: ((Step<*, *>, INPUT) -> Unit)? = null
    var onOutInterceptor: ((Step<*, *>, INPUT, OUTPUT) -> Unit)? = null
    var onExceptionInterceptor: ((Step<*, *>, Exception) -> Unit)? = null
    var afterStepInterceptor: ((State, Step<*, *>) -> Unit)? = null
    var afterCompletionInterceptor: ((FINALISED_STATE) -> Unit)? = null

    fun beforeStart(
        interceptor: (TRANSIENT_STATE, Step<*, *>) -> Unit
    ) {
        beforeStartInterceptor = interceptor
    }

    fun beforeStep(
        interceptor: (TRANSIENT_STATE, Step<*, *>) -> Unit
    ) {
        beforeStepInterceptor = interceptor
    }

    fun onIn(
        interceptor: (Step<*, *>, INPUT) -> Unit
    ) {
        onInInterceptor = interceptor
    }

    fun onOut(
        interceptor: (Step<*, *>, INPUT, OUTPUT) -> Unit
    ) {
        onOutInterceptor = interceptor
    }

    fun onException(
        interceptor: (Step<*, *>, Exception) -> Unit
    ) {
        onExceptionInterceptor = interceptor
    }

    fun afterStep(
        interceptor: (State, Step<*, *>) -> Unit
    ) {
        afterStepInterceptor = interceptor
    }

    fun afterCompletion(
        interceptor: (FINALISED_STATE) -> Unit
    ) {
        afterCompletionInterceptor = interceptor
    }

    fun build(): OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
        return object : OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
            override fun beforeStart(
                initialState: TRANSIENT_STATE,
                initialStep: Step<*, *>
            ) {
                beforeStartInterceptor?.invoke(initialState, initialStep)
            }

            override fun beforeStep(
                state: TRANSIENT_STATE,
                step: Step<*, *>
            ) {
                beforeStepInterceptor?.invoke(state, step)
            }

            override fun onIn(step: Step<*, *>, input: INPUT) {
                onInInterceptor?.invoke(step, input)
            }

            override fun onOut(
                step: Step<*, *>,
                input: INPUT,
                output: OUTPUT
            ) {
                onOutInterceptor?.invoke(step, input, output)
            }

            override fun onException(
                step: Step<*, *>,
                exception: Exception
            ) {
                onExceptionInterceptor?.invoke(step, exception)
            }

            override fun afterStep(
                state: State,
                step: Step<*, *>,
            ) {
                afterStepInterceptor?.invoke(state, step)
            }

            override fun afterCompletion(finalState: FINALISED_STATE) {
                afterCompletionInterceptor?.invoke(finalState)
            }

        }
    }
}