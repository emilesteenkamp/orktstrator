package me.emilesteenkamp.orktestrator.core

interface StepInterceptor<INPUT, OUTPUT> {
    fun onIn(input: INPUT)

    fun onOut(input: INPUT, output: OUTPUT)

    fun onException(exception: Exception)
}