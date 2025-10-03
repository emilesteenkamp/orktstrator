package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

internal class GraphCore<TRANSIENT_STATE, FINALISED_STATE>(
    private val entryPoint: Step<*, *>,
    private val map: Map<Step<*, *>, StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>>,
) : Graph<TRANSIENT_STATE, FINALISED_STATE> where TRANSIENT_STATE : State.Transient,
                                                  FINALISED_STATE : State.Final {
    override fun entryPoint() = entryPoint

    override fun <INPUT, OUTPUT> engineFor(step: Step<INPUT, OUTPUT>) =
        map[step]?.unsafeCast<INPUT, OUTPUT>()

    /**
     * Our [GraphBuilder] is ensuring that the [INPUT] and [OUTPUT] generic types should match for any given [Map.Entry]
     * [Step] key and [StepEngine] value, therefor we are confident to make this unchecked cast at runtime.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <INPUT, OUTPUT> StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>.unsafeCast() =
        this as StepEngine<TRANSIENT_STATE, FINALISED_STATE, INPUT, OUTPUT>
}