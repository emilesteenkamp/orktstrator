package me.emilesteenkamp.orktestrator.definition

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.OrktestratorException
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step
import me.emilesteenkamp.orktestrator.core.Graph
import me.emilesteenkamp.orktestrator.core.StepInterceptor
import me.emilesteenkamp.orktestrator.core.from
import me.emilesteenkamp.orktestrator.core.builder
import me.emilesteenkamp.orktestrator.core.withStepInterceptor

class OrktestratorDefinition<TRANSIENT_STATE, FINALISED_STATE>
internal constructor()
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    private val graphBuilder = Graph.builder<TRANSIENT_STATE, FINALISED_STATE>()
    private val stepInterceptorList = mutableListOf<StepInterceptor<*, *>>()

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> step(
        step: Step<INPUT, OUTPUT>,
        collector: CollectorScope.(TRANSIENT_STATE) -> INPUT,
        modifier: ((TRANSIENT_STATE, OUTPUT) -> State)? = null,
        router: ((TRANSIENT_STATE) -> Step<*, *>)? = null,
        executor: suspend (INPUT) -> OUTPUT,
    ) {
        graphBuilder.add(
            stepDefinition = Graph.Builder.StepDefinition(
                step = step,
                collector = collector,
                modifier = modifier,
                router = router,
                executor = executor
            )
        )
    }

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> intercept(definition: StepInterceptorDefinition<INPUT, OUTPUT>.() -> Unit) {
        stepInterceptorList.add(StepInterceptorDefinition<INPUT, OUTPUT>().apply(definition).build())
    }

    @Throws(OrktestratorException.DefinitionException::class)
    internal fun build(): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> {
        return Orktestrator
            .from(graphBuilder.build())
            .apply {
                stepInterceptorList.fold(this) { orktestrator, stepInterceptor ->
                    orktestrator.withStepInterceptor(stepInterceptor)
                }
            }
    }
}
