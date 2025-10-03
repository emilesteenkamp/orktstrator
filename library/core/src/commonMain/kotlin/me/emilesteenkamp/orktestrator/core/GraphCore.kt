package me.emilesteenkamp.orktestrator.core

import me.emilesteenkamp.orktestrator.api.State
import me.emilesteenkamp.orktestrator.api.Step

internal class GraphCore<TRANSIENT_STATE, FINALISED_STATE>
private constructor(
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

    companion object {
        /**
         * We use [GraphBuilder] as a receiver here to ensure that we can only construct an instance of [GraphCore]
         * through our [GraphBuilder].
         */
        @Suppress("UnusedReceiverParameter")
        fun <TRANSIENT_STATE, FINALISED_STATE> GraphBuilder<TRANSIENT_STATE, FINALISED_STATE>.construct(
            entryPoint: Step<*, *>,
            map: Map<Step<*, *>, StepEngine<TRANSIENT_STATE, FINALISED_STATE, *, *>>
        ): GraphCore<TRANSIENT_STATE, FINALISED_STATE>
        where TRANSIENT_STATE : State.Transient,
              FINALISED_STATE : State.Final = GraphCore(entryPoint, map)
    }
}