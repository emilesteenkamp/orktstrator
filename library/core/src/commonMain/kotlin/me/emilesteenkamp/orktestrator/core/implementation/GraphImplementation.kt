package me.emilesteenkamp.orktestrator.core.implementation

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.OrktestratorException
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step
import me.emilesteenkamp.orktestrator.core.Graph

internal class GraphImplementation<TRANSIENT_STATE, FINALISED_STATE>
private constructor(
    private val entryPoint: Step<*, *>,
    private val map: Map<Step<*, *>, Graph.StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>>,
) : Graph<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                  FINALISED_STATE : State.Final {
    override fun entryPoint() = entryPoint

    override fun <INPUT, OUTPUT> engineFor(step: Step<INPUT, OUTPUT>) =
        map[step]?.unsafeCast<INPUT, OUTPUT>()

    /**
     * Our [BuilderImplementation] is ensuring that the [INPUT] and [OUTPUT] generic types should match for any given [Map.Entry]
     * [Step] key and [StepEngine] value, therefor we are confident to make this unchecked cast at runtime.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <INPUT, OUTPUT> Graph.StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>.unsafeCast() =
        this as Graph.StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>

    internal class BuilderImplementation<TRANSIENT_STATE, FINALISED_STATE> : Graph.Builder<TRANSIENT_STATE, FINALISED_STATE>
            where TRANSIENT_STATE : State.Transient,
                  FINALISED_STATE : State.Final
    {
        private val stepDefinitionList = mutableListOf<Graph.Builder.StepDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

        override fun <INPUT, OUTPUT> add(stepDefinition: Graph.Builder.StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>) {
            stepDefinitionList.add(stepDefinition)
        }

        override fun build(): Graph<TRANSIENT_STATE, FINALISED_STATE> = GraphImplementation(
            entryPoint = stepDefinitionList.firstOrNull()?.step
                ?: throw OrktestratorException.DefinitionException.NoStepsDefined(),
            map = stepDefinitionList
                .windowed(2, 1, partialWindows = true) { window ->
                    window[0].step to toStepEngine(window[0], window.getOrNull(1))
                }.toMap()
        )

        private fun <INPUT, OUTPUT> toStepEngine(
            current: Graph.Builder.StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>,
            next: Graph.Builder.StepDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>?
        ): Graph.StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
            return object : Graph.StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
                override fun CollectorScope.collector(state: TRANSIENT_STATE): INPUT =
                    current.collector.invoke(this, state)

                override fun modifier(state: TRANSIENT_STATE, output: OUTPUT) =
                    current.modifier?.invoke(state, output) ?: state

                override fun router(state: TRANSIENT_STATE) =
                    current.router?.invoke(state) ?: next?.step

                override suspend fun executor(input: INPUT) =
                    current.executor.invoke(input)
            }
        }
    }
}