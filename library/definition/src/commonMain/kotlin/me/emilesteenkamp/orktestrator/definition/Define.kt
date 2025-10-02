package me.emilesteenkamp.orktestrator.definition

import me.emilesteenkamp.orktestrator.api.Orktestrator

@Suppress("UNUSED")
fun <TRANSIENT_STATE, FINALISED_STATE> Orktestrator.Companion.define(
    builder: OrktestratorBuilder<TRANSIENT_STATE, FINALISED_STATE>.() -> Unit,
): Orktestrator<TRANSIENT_STATE, FINALISED_STATE> where
        TRANSIENT_STATE : Orktestrator.State.Transient,
        FINALISED_STATE : Orktestrator.State.Final =
    OrktestratorBuilder<TRANSIENT_STATE, FINALISED_STATE>().apply(builder).build()