package me.emilesteenkamp.orktstrator.definition

import io.kotest.matchers.collections.shouldHaveSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import me.emilesteenkamp.orktstrator.api.Orktstrator
import me.emilesteenkamp.orktstrator.api.State
import me.emilesteenkamp.orktstrator.api.Step

class OrktstratorDefinitionTest {
    @Test
    fun `Test counter`() = runTest {
        val stepCapturer = StepCapturer<CounterState.Transient, CounterState.Final>()

        Orktstrator.define {
            capturePerformedStep(stepCapturer)
            step(
                Increment,
                collector = { state -> Increment.Input(counter = state.counter) },
                modifier = { state, output ->
                    state.copy(counter = output.counter)
                        .takeIf { it.counter < 20 }
                        ?: CounterState.Final(output.counter)
                },
                router = {
                    Increment
                }
            ) { input ->
                Increment.Output(input.counter + 1)
            }
        }.orchestrate(CounterState.Transient(counter = 0))

        stepCapturer.performedStepList() shouldHaveSize 20
    }

    object CounterState {
        data class Transient(val counter: Int) : State.Transient

        data class Final(val counter: Int) : State.Final
    }

    object Increment : Step<Increment.Input, Increment.Output> {
        class Input(val counter: Int)

        class Output(val counter: Int)
    }
}