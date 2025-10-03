package me.emilesteenkamp.orktestrator.definition

import me.emilesteenkamp.orktestrator.api.Orktestrator
import me.emilesteenkamp.orktestrator.api.State

@Suppress("UNUSED")
fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator.Companion.define(
    builder: OrktestratorDefinition<TRANSIENT_STATE, FINALISED_STATE>.() -> Unit,
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where
        TRANSIENT_STATE : State.Transient,
        FINALISED_STATE : State.Final =
    OrktestratorDefinition<TRANSIENT_STATE, FINALISED_STATE>().apply(builder).build()