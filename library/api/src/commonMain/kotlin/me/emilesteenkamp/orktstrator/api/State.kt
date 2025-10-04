package me.emilesteenkamp.orktstrator.api

sealed interface State {
    interface Transient : State

    interface Final : State
}