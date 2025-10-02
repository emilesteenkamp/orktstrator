package me.emilesteenkamp.orktestrator.builder

import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.core.OrktestratorCore

class OrktestratorBuilder<TRANSIENT_STATE, FINALISED_STATE>
internal constructor()
        where TRANSIENT_STATE : Orktestrator.State.Transient,
              FINALISED_STATE : Orktestrator.State.Final {
    private val graphBuilder = linkedMapOf<Orktestrator.Step<*, *>, StepRunnerDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

    @Suppress("UNUSED")
    fun <INPUT, OUTPUT> step(
        step: Orktestrator.Step<INPUT, OUTPUT>,
        collector: Orktestrator.CollectorScope.(TRANSIENT_STATE) -> INPUT,
        modifier: ((TRANSIENT_STATE, OUTPUT) -> Orktestrator.State)? = null,
        router: ((TRANSIENT_STATE) -> Orktestrator.Step<*, *>)? = null,
        executor: suspend (INPUT) -> OUTPUT,
    ) {
        graphBuilder[step] =
            StepRunnerDefinition(
                collector = collector,
                modifier = modifier,
                router = router,
                executor = executor,
            )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun build(): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> {
        val graphMap = linkedMapOf<Orktestrator.Step<*, *>, OrktestratorCore.StepRunner<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

        graphBuilder.entries.forEachIndexed { index, entry ->
            graphMap[entry.key] =
                OrktestratorCore.StepRunner(
                    collector = entry.value.collector as Orktestrator.CollectorScope.(TRANSIENT_STATE) -> Any,
                    modifier = (entry.value.modifier ?: { state, _ -> state }) as (TRANSIENT_STATE, Any) -> Orktestrator.State,
                    router =
                        entry.value.router ?: { _ ->
                            graphBuilder
                                .entries
                                .elementAtOrNull(index + 1)
                                ?.key
                                ?: Orktestrator.Step.None
                        },
                    executor = entry.value.executor as suspend (Any) -> Any,
                )
        }

        return OrktestratorCore(OrktestratorCore.Graph(graphMap))
    }

    private data class StepRunnerDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>(
        val collector: Orktestrator.CollectorScope.(TRANSIENT_STATE) -> INPUT,
        val modifier: ((TRANSIENT_STATE, OUTPUT) -> Orktestrator.State)? = null,
        val router: ((TRANSIENT_STATE) -> (Orktestrator.Step<*, *>))? = null,
        val executor: suspend (INPUT) -> OUTPUT,
    ) where TRANSIENT_STATE : Orktestrator.State.Transient,
            FINALISED_STATE : Orktestrator.State.Final
}

@Suppress("UNUSED")
fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator.Companion.define(
    builder: OrktestratorBuilder<TRANSIENT_STATE, FINALISED_STATE>.() -> Unit,
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where
        TRANSIENT_STATE : Orktestrator.State.Transient,
        FINALISED_STATE : Orktestrator.State.Final =
    OrktestratorBuilder<TRANSIENT_STATE, FINALISED_STATE>().apply(builder).build()