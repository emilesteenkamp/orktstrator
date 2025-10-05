package me.emilesteenkamp.orktstrator.definition

import me.emilesteenkamp.orktstrator.api.CollectorScope
import me.emilesteenkamp.orktstrator.api.Orktstrator
import me.emilesteenkamp.orktstrator.api.OrktstratorException
import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step
import me.emilesteenkamp.orktstrator.core.OrktstratorGraph
import me.emilesteenkamp.orktstrator.core.OrktstratorInterceptor
import me.emilesteenkamp.orktstrator.core.from
import me.emilesteenkamp.orktstrator.core.builder
import me.emilesteenkamp.orktstrator.core.withStepInterceptor

class OrktstratorDefinition<TRANSIENT_STATE, FINALISED_STATE>
internal constructor()
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    private val orktstratorGraphBuilder = OrktstratorGraph.builder<TRANSIENT_STATE, FINALISED_STATE>()
    private val orktstratorInterceptorList = mutableListOf<OrktstratorInterceptor<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> step(
        step: Step<INPUT, OUTPUT>,
        collector: CollectorScope.(TRANSIENT_STATE) -> INPUT,
        modifier: ((TRANSIENT_STATE, OUTPUT) -> State)? = null,
        router: ((TRANSIENT_STATE) -> Step<*, *>)? = null,
        executor: suspend (INPUT) -> OUTPUT,
    ) {
        orktstratorGraphBuilder.add(
            stepDefinition = OrktstratorGraph.Builder.StepDefinition(
                step = step,
                collector = collector,
                modifier = modifier,
                router = router,
                executor = executor
            )
        )
    }

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> intercept(
        definition: StepInterceptorDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>.() -> Unit
    ) {
        StepInterceptorDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>()
            .apply(definition)
            .build()
            .also { orktstratorInterceptorList.add(it) }
    }

    @Throws(OrktstratorException.DefinitionException::class)
    internal fun build(): Orktstrator<TRANSIENT_STATE, FINALISED_STATE> {
        return Orktstrator
            .from(orktstratorGraphBuilder.build())
            .let {
                orktstratorInterceptorList.fold(it) { orktstrator, stepInterceptor ->
                    orktstrator.withStepInterceptor(stepInterceptor)
                }
            }
    }
}
