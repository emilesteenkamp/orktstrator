package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.CollectorScope
import me.emilesteenkamp.orktestrator.api.OrktestratorException
import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

interface Graph<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                        FINALISED_STATE : State.Final
{
    fun entryPoint(): Step<*, *>

    fun <INPUT, OUTPUT> engineFor(step: Step<INPUT, OUTPUT>): StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>?

    interface StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>
            where TRANSIENT_STATE : State.Transient,
                  FINALISED_STATE : State.Final
    {
        @Throws(OrktestratorException.OrchestrationException.RequiredValueMissing::class)
        fun CollectorScope.collector(state: TRANSIENT_STATE): INPUT
        fun modifier(state: TRANSIENT_STATE, output: OUTPUT): State
        fun router(state: TRANSIENT_STATE): Step<*, *>?
        suspend fun executor(input: INPUT): OUTPUT
    }

    interface Builder<TRANSIENT_STATE, FINALISED_STATE>
            where TRANSIENT_STATE : State.Transient,
                  FINALISED_STATE : State.Final
    {
        fun <INPUT, OUTPUT> add(stepDefinition: StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>)

        fun build(): Graph<TRANSIENT_STATE, FINALISED_STATE>

        data class StepDefinition<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>(
            val step: Step<INPUT, OUTPUT>,
            val collector: CollectorScope.(TRANSIENT_STATE) -> INPUT,
            val modifier: ((TRANSIENT_STATE, OUTPUT) -> State)?,
            val router: ((TRANSIENT_STATE) -> Step<*, *>)?,
            val executor: suspend (INPUT) -> OUTPUT
        ) where TRANSIENT_STATE : State.Transient,
                FINALISED_STATE : State.Final
    }

    companion object
}