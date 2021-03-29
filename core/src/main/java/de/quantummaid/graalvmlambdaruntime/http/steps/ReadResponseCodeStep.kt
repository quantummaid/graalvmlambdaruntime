package de.quantummaid.graalvmlambdaruntime.http.steps

import de.quantummaid.graalvmlambdaruntime.http.connectionSettingsAsMap
import java.net.HttpURLConnection

/**
 * Besides reading the response code it will throw a LambdaEnvironmentTainted exception
 * if anything went wrong. This leads to logging the error and killing the lambda instance.
 */
class ReadResponseCodeStep(private val connection: HttpURLConnection) : Step<Int>("ReadResponseCode") {
    override fun execute(): StepResult<Int> {
        try {
            val responseCode = connection.responseCode
            return if (responseCode in 200..299) {
                success(responseCode)
            } else {
                error("Unsupported response code.",
                    "responseCode" to responseCode.toString(),
                    *connection.connectionSettingsAsMap()
                )
            }
        } catch (e: Exception) {
            return dealBreaker(
                "Exception receiving response code.",
                "exception" to e.stackTraceToString(),
                *connection.connectionSettingsAsMap()
            )
        }
    }
}
