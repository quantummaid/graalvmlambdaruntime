package de.quantummaid.graalvmlambdaruntime.http

import de.quantummaid.graalvmlambdaruntime.LambdaEnvironmentTainted.Companion.lambdaEnvironmentTainted
import de.quantummaid.graalvmlambdaruntime.http.steps.*
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import java.net.URL

/**
 * @see https://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html for details on connection reuse
 */
class LambdaInvocationEndpointSdkClientUtils {
    companion object {
        fun doHttpCall(url: URL,
                       method: String,
                       readTimeout: Int,
                       metrics: PerformanceMetrics,
                       logger: StructuralLogger,
                       resultPayload: String? = null): JavaSdkHttpClientResult {
            return metrics.addMetric("DoHttpCall") {
                val stepper = Stepper(name = "${method} ${url}", exceptionFactory = {
                    lambdaEnvironmentTainted("Error executing http call", *it)
                }, logger)
                val connection = stepper.stepRequired(
                        CreateHttpURLConnectionStep(
                                url,
                                readTimeout,
                                method,
                                resultPayload
                        ),
                        metrics
                )
                val responseCodeResult = stepper.step(ReadResponseCodeStep(connection), metrics)
                val headerFieldsResult = stepper.step(ReadHeaderMapStep(connection), metrics)
                val normalResponseTextResult = stepper.step(ReadStreamStep(connection, "input") { it.inputStream }, metrics)
                val errorResponseTextResult = stepper.step(ReadStreamStep(connection, "error") { it.errorStream }, metrics)

                stepper.assertStepsFinishedWithoutError(metrics)
                JavaSdkHttpClientResult(
                        responseCodeResult.successValue(),
                        headerFieldsResult.successValue(),
                        normalResponseTextResult.successValue(),
                        errorResponseTextResult.successValue()
                )
            }
        }
    }
}

data class JavaSdkHttpClientResult(
        val responseCode: Int,
        val headerFields: Map<String, List<String>>,
        val normalResponseText: String?,
        val errorResponseText: String?,
) {
    fun firstHeaderValue(headerName: String): String {
        val headerValueCollection = headerFields.keys
                .firstOrNull { it.equals(headerName, ignoreCase = true) }
                ?.let { headerFields[it] }
        when {
            headerValueCollection == null -> {
                throw lambdaEnvironmentTainted("Could not find header ${headerName}",
                        "requiredHeader" to headerName,
                        "availableHeaders" to headerFields
                )
            }
            headerValueCollection.isEmpty() -> {
                throw lambdaEnvironmentTainted("Could find header, though its value collection is empty",
                        "requiredHeader" to headerName,
                        "availableHeaders" to headerFields
                )
            }
            else -> {
                return headerValueCollection.first()
            }
        }
    }
}
