package me.emilesteenkamp.orktestrator.api

sealed class OrktestratorException : Exception() {
    sealed class DefinitionException : OrktestratorException() {
        class NoStepsDefined : DefinitionException()
    }

    sealed class OrchestrationException : OrktestratorException() {
        class RequiredValueMissing : OrchestrationException()

        class InvalidStateModificationResult : OrchestrationException()

        class NoNextStepDefined : OrchestrationException()

        class UndefinedNextStep : OrchestrationException()

        class InvalidInputIntercepted : OrchestrationException()

        class InvalidOutputIntercepted : OrchestrationException()
    }
}