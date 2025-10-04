package me.emilesteenkamp.orktstrator.api

sealed class OrktstratorException : Exception() {
    sealed class DefinitionException : OrktstratorException() {
        class NoStepsDefined : DefinitionException()
    }

    sealed class OrchestrationException : OrktstratorException() {
        class RequiredValueMissing : OrchestrationException()

        class InvalidStateModificationResult : OrchestrationException()

        class NoNextStepDefined : OrchestrationException()

        class UndefinedNextStep : OrchestrationException()

        class InvalidInputIntercepted : OrchestrationException()

        class InvalidOutputIntercepted : OrchestrationException()
    }
}