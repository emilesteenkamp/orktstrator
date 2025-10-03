package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.OrktestratorError
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

class GraphBuilder<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final
{
    private val stepDefinitionList = mutableListOf<StepDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

    fun <INPUT, OUTPUT> add(stepDefinition: StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>) {
        stepDefinitionList.add(stepDefinition)
    }

    fun build(): Graph<TRANSIENT_STATE, FINALISED_STATE> {
        return GraphCore(
            entryPoint = stepDefinitionList.firstOrNull()?.step
                ?: throw OrktestratorError.DefinitionError.NoStepsDefined(),
            map = stepDefinitionList
                .windowed(2, 1, partialWindows = true) { window ->
                    window[0].step to toStepEngine(window[0], window.getOrNull(1))
                }.toMap()
        )
    }

    private fun <INPUT, OUTPUT> toStepEngine(
        current: StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>,
        next: StepDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>?
    ): StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
        return object : StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT> {
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

    data class StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>(
        val step: Step<INPUT, OUTPUT>,
        val collector: CollectorScope.(TRANSIENT_STATE) -> INPUT,
        val modifier: ((TRANSIENT_STATE, OUTPUT) -> State)?,
        val router: ((TRANSIENT_STATE) -> Step<*, *>)?,
        val executor: suspend (INPUT) -> OUTPUT
    ) where TRANSIENT_STATE : State.Transient,
            FINALISED_STATE : State.Final
}