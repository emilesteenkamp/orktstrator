package me.emilesteenkamp.orktstrator.api

object CollectorScope {
    @Suppress("UNUSED")
    @Throws(OrktstratorException.OrchestrationException.RequiredValueMissing::class)
    fun <T : Any?> T?.requireNotNull(): T = this ?: throw OrktstratorException.OrchestrationException.RequiredValueMissing()
}