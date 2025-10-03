package me.emilesteenkamp.orktestrator.api

object CollectorScope {
    @Suppress("UNUSED")
    @Throws(OrktestratorError.RuntimeError.RequiredValueMissing::class)
    fun <T : Any?> T?.requireNotNull(): T = this ?: throw OrktestratorError.RuntimeError.RequiredValueMissing()
}