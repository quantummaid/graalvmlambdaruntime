package de.quantummaid.graalvmlambdaruntime.http.steps

import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging

private val baseLogger = KotlinLogging.logger { }

class Stepper(private val name: String,
              private val exceptionFactory: (ErrorInfoSummary) -> Exception,
              private val logger: StructuralLogger) {
    private val results = mutableListOf<StepResult<*>>()
    private var failed = false

    fun <T> stepRequired(step: Step<T>,
                         metrics: PerformanceMetrics): T {
        return metrics.addMetric("Stepper${name}.${step.name}") {
            val result = step.execute()
            results.add(result)
            when (result) {
                is StepResult.Success -> {
                    logger.traceLogMap(baseLogger) {
                        mapOf(
                            "message" to "${result.stepName} succeeded with result ${result.successResult}"
                        )
                    }
                    result.successResult
                }
                is StepResult.Error -> {
                    failed = true
                    val merged = arrayOf(
                            *errorInfoSummary(),
                            "Stepper.Message" to "Required step failed, escalating step error to deal breaker"
                    )
                    throw exceptionFactory(merged)
                }
                is StepResult.DealBreaker -> {
                    failed = true
                    throw exceptionFactory(errorInfoSummary())
                }
            }
        }
    }

    fun <T> step(step: Step<T>,
                 metrics: PerformanceMetrics): StepResult<T> {
        return metrics.addMetric("Stepper${name}.${step.name}") {
            val result = step.execute()
            results.add(result)
            if (result is StepResult.Error) {
                failed = true
                if (result is StepResult.DealBreaker) {
                    throw exceptionFactory(errorInfoSummary())
                }
            }
            result
        }
    }

    fun assertStepsFinishedWithoutError(metrics: PerformanceMetrics) {
        metrics.addMetric("Stepper${name}.assertStepsFinishedWithoutError") {
            logger.traceLogMap(baseLogger) {
                mapOf(
                    "message" to "Stepper ${name} finished",
                    "status" to if (failed) "failed" else "success"
                )
            }
            if (failed) {
                throw exceptionFactory(errorInfoSummary())
            }
        }
    }

    private fun errorInfoSummary(): ErrorInfoSummary {
        return results.flatMap { result ->
            when (result) {
                is StepResult.Success -> listOf("${result.stepName}.SuccessResult" to result.successResult.toString())
                is StepResult.Error -> {
                    result.errorInfo.map { "${result.stepName}.${it.first}" to it.second }
                }
            }
        }.toTypedArray()
    }
}

typealias ErrorInfoSummary = Array<out Pair<String, String>>

abstract class Step<T>(val name: String) {
    fun success(value: T): StepResult.Success<T> {
        return StepResult.Success(name, value)
    }

    fun error(message: String,
              vararg errorInfo: Pair<String, String>): StepResult.Error<T> {
        return StepResult.Error(name, arrayOf(
                "message" to message,
                *errorInfo
        ))
    }

    fun dealBreaker(message: String,
                    vararg errorInfo: Pair<String, String>): StepResult.DealBreaker<T> {
        return StepResult.DealBreaker(name, arrayOf(
                "message" to message,
                *errorInfo
        ))
    }

    abstract fun execute(): StepResult<T>
}

sealed class StepResult<T> {
    open fun successValue(): T {
        throw UnsupportedOperationException("${this} has no success value")
    }

    class Success<T>(val stepName: String,
                     val successResult: T) : StepResult<T>() {
        override fun successValue(): T {
            return successResult
        }
    }

    open class Error<T>(val stepName: String,
                        val errorInfo: Array<out Pair<String, String>>) : StepResult<T>()

    class DealBreaker<T>(stepName: String,
                         errorInfo: Array<out Pair<String, String>>) : Error<T>(stepName, errorInfo)
}
