package me.emilesteenkamp.orktestrator.definition

import me.emilesteenkamp.orktestrator.core.StepInterceptor

class StepInterceptorDefinition<INPUT, OUTPUT> internal constructor() {
    var inInterceptor: ((INPUT) -> Unit) = {  }
    var outInterceptor: ((INPUT, OUTPUT) -> Unit) = { _, _ -> }
    var exceptionInterceptor: ((Exception) -> Unit) = {  }

    @Suppress("UNUSED")
    fun onIn(inInterceptor: (INPUT) -> Unit) {
        this.inInterceptor = inInterceptor
    }

    @Suppress("UNUSED")
    fun onOut(outInterceptor: (INPUT, OUTPUT) -> Unit) {
        this.outInterceptor = outInterceptor
    }

    @Suppress("UNUSED")
    fun onException(exceptionInterceptor: (Exception) -> Unit) {
        this.exceptionInterceptor = exceptionInterceptor
    }

    fun build(): StepInterceptor<INPUT, OUTPUT> {
        return object : StepInterceptor<INPUT, OUTPUT> {
            override fun onIn(input: INPUT) =
                inInterceptor(input)

            override fun onOut(input: INPUT, output: OUTPUT) =
                outInterceptor(input, output)

            override fun onException(exception: Exception) =
                exceptionInterceptor(exception)

        }
    }
}