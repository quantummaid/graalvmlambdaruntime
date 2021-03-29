package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.http.LambdaInvocationEndpointSdkClientUtils
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging
import java.net.URL

private val baseLogger = KotlinLogging.logger { }

/**
 * Do not use a read timeout on the GET call (set to -0).
 * Between when Lambda bootstraps the runtime and when the runtime has an
 * event to return, the runtime process may be frozen for several seconds.
 */
class NextInvocation {
    companion object {
        fun nextInvocation(url: URL, metrics: PerformanceMetrics, logger: StructuralLogger): Invocation {
            return metrics.addMetric("RetrieveNextInvocation") {
                val result = LambdaInvocationEndpointSdkClientUtils.doHttpCall(url, "GET", 0, metrics, logger)
                val lambdaRuntimeAwsRequestId = result.firstHeaderValue("Lambda-Runtime-Aws-Request-Id")
                val contentType = result.firstHeaderValue("Content-Type")
                logger.debugLogMap(baseLogger) {
                    mapOf(
                            "message" to "Received next invocation",
                            "lambdaRuntimeAwsRequestId" to lambdaRuntimeAwsRequestId,
                            "contentType" to contentType,
                            "payload" to result.normalResponseText
                    )
                }
                Invocation(lambdaRuntimeAwsRequestId, contentType, result.normalResponseText)
            }
        }
    }
}

data class Invocation(
    val lambdaRuntimeAwsRequestId: String,
    val contentType: String?,
    val payload: String?
)
