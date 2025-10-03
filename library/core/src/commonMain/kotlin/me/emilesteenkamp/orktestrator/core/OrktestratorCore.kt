package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.OrktestratorError
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

class OrktestratorCore<TRANSIENT_STATE, FINALISED_STATE>(
    private val graph: Graph<TRANSIENT_STATE, FINALISED_STATE>,
) : Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                         FINALISED_STATE : State.Final {
    @Throws(OrktestratorError.RuntimeError::class)
    override suspend fun start(initialState: TRANSIENT_STATE): FINALISED_STATE = run(
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

    class Graph<TRANSIENT_STATE, FINALISED_STATE>
    private constructor(
        private val entryPoint: Step<*, *>,
        private val map: Map<Step<*, *>, StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>>,
    ) where TRANSIENT_STATE : State.Transient,
            FINALISED_STATE : State.Final {
        fun entryPoint() = entryPoint


        fun <INPUT, OUTPUT> engineFor(step: Step<INPUT, OUTPUT>) =
            map[step]?.unsafeCast<INPUT, OUTPUT>()

        @Suppress("UNCHECKED_CAST")
        private fun <INPUT, OUTPUT> StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>.unsafeCast() =
            this as StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>

        class Builder<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                              FINALISED_STATE : State.Final
        {
            private val stepDefinitionList = mutableListOf<StepDefinition<TRANSIENT_STATE, FINALISED_STATE, *, *>>()

            fun <INPUT, OUTPUT> add(stepDefinition: StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>) {
                stepDefinitionList.add(stepDefinition)
            }

            fun build(): Graph<TRANSIENT_STATE, FINALISED_STATE> {
                return Graph(
                    entryPoint = stepDefinitionList.firstOrNull()?.step ?: throw OrktestratorError.DefinitionError.NoStepsDefined(),
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
    }

    interface StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>
            where TRANSIENT_STATE : State.Transient,
                  FINALISED_STATE : State.Final
    {
        @Throws(OrktestratorError.RuntimeError.RequiredValueMissing::class)
        fun CollectorScope.collector(state: TRANSIENT_STATE): INPUT
        fun modifier(state: TRANSIENT_STATE, output: OUTPUT): State
        fun router(state: TRANSIENT_STATE): Step<*, *>?
        suspend fun executor(input: INPUT): OUTPUT
    }
}