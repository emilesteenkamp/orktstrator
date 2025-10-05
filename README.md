# 🧩 Orktstrator

**Orktstrator** is a lightweight, type-safe **Kotlin Multiplatform orchestration DSL** that lets you define workflows or state machines using simple declarative steps.
It provides a composable way to describe sequential, branching, or conditional logic in a clean, Kotlin-idiomatic way.

---

## 🚀 Features

* 🧠 **Type-safe DSL** for defining workflow steps
* 🔄 **Deterministic execution** — each step defines its own `collector`, `modifier`, `router`, and `executor`
* 🪶 **Lightweight** — no reflection, minimal overhead
* 🌍 **Multiplatform** — works on JVM, JS, iOS, Linux, macOS, and Windows
* 🧩 **Composable design** — build workflows from small, reusable steps
* ⚙️ **Extensible** — interceptors and hooks for logging, debugging, or analytics

---

## 📦 Installation

Add the GitHub Packages repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/emilesteenkamp/orktstrator")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("me.emilesteenkamp.orktstrator:orktstrator-core:<version>")
}
```

Replace `<version>` with the latest release (e.g. `0.1.0` or `0.1.0-SNAPSHOT`).

---

## 🧱 Module Overview

| Module             | Description                                             | Published | Artifact           |
| ------------------ | ------------------------------------------------------- | --------- | ------------------ |
| **api**            | Core interfaces (`Orktstrator`, `State`, `Step`, etc.) | ✅         | `orktstrator-api`  |
| **core**           | The execution engine and DSL runtime                    | ✅         | `orktstrator-core` |
| **implementation** | Internal helpers and integrations                       | 🚫        | *(not published)*  |

Consumers normally depend on **`orktstrator-core`**, which transitively includes `api`.

---

## 🧩 Quick Example

Here’s a minimal example showing how to define and run a workflow:

```kotlin
val ork = Orktstrator.define<MyState.Transient, MyState.Final> {
    step(
        ValidateInput,
        collector = { state -> ValidateInput.Input(state.data) },
        modifier = { state, output ->
            if (output.isValid) state else MyState.Final.Error("Invalid input")
        },
        router = { state ->
            if (state.shouldAuthenticate) Authenticate else Complete
        }
    ) { input ->
        ValidateInput.Output(isValid = input.data.isNotEmpty())
    }

    step(
        Authenticate,
        collector = { state -> Authenticate.Input(state.credentials) },
        modifier = { state, output ->
            if (output.success) state.copy(token = output.token)
            else MyState.Final.Error("Authentication failed")
        },
        router = { Complete }
    ) { input ->
        Authenticate.Output(success = true, token = "abc123")
    }

    step(
        Complete,
        collector = { state -> Complete.Input(state.token!!) },
        modifier = { _, _ -> MyState.Final.Success },
        router = { Step.None }
    ) { input ->
        Complete.Output("done")
    }
}

val result = ork.orchestrate(MyState.Transient(data = "hello"))
println(result)
```

---

## ⚙️ Core Concepts

| Concept       | Description                                                                           |
| ------------- | ------------------------------------------------------------------------------------- |
| **Step**      | The smallest unit of work in a workflow. Defines input, output, and transition logic. |
| **Collector** | Builds the step’s input from the current state.                                       |
| **Executor**  | Executes the business logic for a step (suspend function).                            |
| **Modifier**  | Transforms state based on the output.                                                 |
| **Router**    | Determines the next step to execute.                                                  |
| **State**     | Represents workflow state; can be `Transient` (in progress) or `Final` (completed).   |

---

## 🧩 Interceptors

You can attach interceptors to observe workflow execution:

```kotlin
class LoggingInterceptor : StepInterceptor<Any, Any> {
    override fun onIn(input: Any) = println("→ $input")
    override fun onOut(input: Any, output: Any) = println("← $output")
    override fun onException(exception: Exception) = println("⚠️ ${exception.message}")
}
```

Attach it when defining your Orktstrator:

```kotlin
intercept<Any, Any> {
    beforeStep { step, input -> println("Starting $step") }
    afterStep { step, output -> println("Finished $step") }
}
```

---

## 🧩 Example Use Cases

* Workflow engines (booking systems, order processing)
* State machines (authentication flows, finite transitions)
* Orchestration pipelines (data or network tasks)
* Testing deterministic processes

---

## 🧰 Requirements

* Kotlin 2.0+
* Works with JVM 21+, JS (IR), and all major KMP native targets.

---

## 🧑‍💻 License

MIT License © 2025 [Emile Steenkamp](https://github.com/emilesteenkamp)

---

Would you like me to also generate **API-level KDoc markdown** (like a `docs/REFERENCE.md`) that auto-documents your `Orktstrator`, `Step`, and `State` interfaces with examples and diagrams?
That would pair nicely with this README for your GitHub repository.
