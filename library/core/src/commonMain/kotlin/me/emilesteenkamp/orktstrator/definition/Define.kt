package me.emilesteenkamp.orktstrator.definition

import me.emilesteenkamp.orktstrator.api.Orktstrator
import me.emilesteenkamp.orktstrator.api.OrktstratorException
import me.emilesteenkamp.orktstrator.api.State

@Suppress("UNUSED")
@Throws(OrktstratorException.DefinitionException::class)
fun <TRANSIENT_STATE, FINALISED_STATE> Orktstrator.Companion.define(
    builder: OrktstratorDefinition<TRANSIENT_STATE, FINALISED_STATE>.() -> Unit,
): Orktstrator<TRANSIENT_STATE, FINALISED_STATE> where
        TRANSIENT_STATE : State.Transient,
        FINALISED_STATE : State.Final =
    OrktstratorDefinition<TRANSIENT_STATE, FINALISED_STATE>().apply(builder).build()