package me.emilesteenkamp.orktestrator.api

object CollectorScope {
    @Suppress("UNUSED")
    @Throws(OrktestratorException.OrchestrationException.RequiredValueMissing::class)
    fun <T : Any?> T?.requireNotNull(): T = this ?: throw OrktestratorException.OrchestrationException.RequiredValueMissing()
}