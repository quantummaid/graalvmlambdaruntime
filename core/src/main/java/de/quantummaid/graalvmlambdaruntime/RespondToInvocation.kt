package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.http.LambdaInvocationEndpointSdkClientUtils
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging

private val baseLogger = KotlinLogging.logger { }

class RespondToInvocation {
    companion object {

        fun respond(invocation: Invocation,
                    configuration: LambdaRuntimeConfiguration,
                    metrics: PerformanceMetrics,
                    logger: StructuralLogger,
                    resultPayload: String?
        ) {
            metrics.addMetric("DeliverInvocationResult") {
                val url = configuration.responseUrl(invocation.lambdaRuntimeAwsRequestId)
                val result = LambdaInvocationEndpointSdkClientUtils.doHttpCall(
                        url,
                        "POST",
                        1000,
                        metrics,
                        logger,
                        resultPayload
                )
                logger.debugLogMap(baseLogger) {
                    mapOf(
                            "message" to "Delivered invocation result",
                            "lambdaRuntimeAwsRequestId" to invocation.lambdaRuntimeAwsRequestId,
                            "contentType" to invocation.contentType,
                            "lambdaEndpointResponseText" to result.normalResponseText,
                            "invocationPayload" to invocation.payload,
                            "resultPayload" to resultPayload
                    )
                }
            }
        }
    }
}
