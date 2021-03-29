package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.marshalling.MapMaidMarshallerAndUnmarshaller.Companion.defaultMarshallerAndUnmarshaller
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging
import java.net.URL
import kotlin.system.exitProcess

private val baseLogger = KotlinLogging.logger { }

class GraalVmLambdaRuntime(val configuration: LambdaRuntimeConfiguration) {

    fun start(handler: LambdaHandler) {
        val environmentVariables = configuration.environmentVariables()
        val marshallerAndUnmarshaller = configuration.marshallerAndUnmarshaller()
        val logger = StructuralLogger(marshallerAndUnmarshaller)

        logger.debugLogMap(baseLogger) {
            mapOf(
                    "Message" to "Main entered in environment",
                    "Environment" to environmentVariables.environmentVariableMap
            )
        }

        val nextInvocationUrl = configuration.nextInvocationUrl()

        try {
            while (true) {
                PerformanceMetrics.startMetering("Lambda Invocation", logger).use { invocationMetrics ->
                    val nextInvocation = NextInvocation.nextInvocation(nextInvocationUrl, invocationMetrics, logger)
                    val event = nextInvocation.payload
                            ?.let { marshallerAndUnmarshaller.unmarshal(it) }
                            ?: emptyMap()
                    val response = handler.handle(event)
                    val responsePayload = marshallerAndUnmarshaller.marshal(response)
                    RespondToInvocation.respond(nextInvocation, configuration, invocationMetrics, logger, responsePayload)
                }
            }
        } catch (e: LambdaEnvironmentTainted) {
            logger.errorLogMap(baseLogger) { e.errorDetails }
            exitProcess(1)
        }
        // No need to catch generic exceptions, the jvm is going to log it stderr and it'll end up in cloudwatch
    }

    companion object {
        @JvmStatic
        fun startGraalVmLambdaRuntime(lambdaHandler: LambdaHandler) {
            val configuration = RealLambdaRuntimeConfiguration()
            startGraalVmLambdaRuntime(configuration, lambdaHandler)
        }

        @JvmStatic
        fun startGraalVmLambdaRuntime(configuration: LambdaRuntimeConfiguration,
                                      lambdaHandler: LambdaHandler) {
            val graalVmLambdaRuntime = GraalVmLambdaRuntime(configuration)
            graalVmLambdaRuntime.start(lambdaHandler)
        }
    }
}

interface LambdaRuntimeConfiguration {
    fun nextInvocationUrl(): URL

    fun responseUrl(requestId: String): URL

    fun marshallerAndUnmarshaller() = defaultMarshallerAndUnmarshaller()

    fun environmentVariables(): LambdaEnvironmentVariables
}

class RealLambdaRuntimeConfiguration : LambdaRuntimeConfiguration {
    private val environmentVariables = LambdaEnvironmentVariables()

    private val nextInvocationUrl = URL(
            "http://${environmentVariables.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/next"
    )

    override fun nextInvocationUrl() = nextInvocationUrl

    override fun responseUrl(requestId: String) = URL(
            "http://${environmentVariables.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/${requestId}/response"
    )

    override fun environmentVariables() = environmentVariables
}

fun interface LambdaHandler {
    fun handle(event: Map<String, Any?>): Map<String, Any?>
}
