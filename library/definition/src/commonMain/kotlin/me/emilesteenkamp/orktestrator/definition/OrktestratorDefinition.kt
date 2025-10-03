package me.emilesteenkamp.orktestrator.definition

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.OrktestratorError
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step
import me.emilesteenkamp.orktestrator.core.OrktestratorCore

class OrktestratorDefinition<TRANSIENT_STATE, FINALISED_STATE>
internal constructor()
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final {
    private val graphBuilder = OrktestratorCore.Graph.Builder<TRANSIENT_STATE, FINALISED_STATE>()

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> step(
        step: Step<INPUT, OUTPUT>,
        collector: CollectorScope.(TRANSIENT_STATE) -> INPUT,
        modifier: ((TRANSIENT_STATE, OUTPUT) -> State)? = null,
        router: ((TRANSIENT_STATE) -> Step<*, *>)? = null,
        executor: suspend (INPUT) -> OUTPUT,
    ) {
        graphBuilder.add(
            stepDefinition = OrktestratorCore.Graph.Builder.StepDefinition(
                step = step,
                collector = collector,
                modifier = modifier,
                router = router,
                executor = executor
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(OrktestratorError.DefinitionError::class)
    internal fun build(): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> {
        return OrktestratorCore(graphBuilder.build())
    }
}
