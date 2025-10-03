package me.emilesteenkamp.orktestrator.api

sealed class OrktestratorError : Error() {
    sealed class DefinitionError : OrktestratorError() {
        class NoStepsDefined : DefinitionError()
    }

    sealed class RuntimeError : OrktestratorError() {
        class RequiredValueMissing : RuntimeError()

        class InvalidStateModificationResult : RuntimeError()

        class NoNextStepDefined : RuntimeError()

        class UndefinedNextStep : RuntimeError()
    }
}