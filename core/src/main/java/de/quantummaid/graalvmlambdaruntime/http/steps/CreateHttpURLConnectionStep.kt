package de.quantummaid.graalvmlambdaruntime.http.steps

import java.net.HttpURLConnection
import java.net.URL

class CreateHttpURLConnectionStep(private val url: URL,
                                  private val readTimeout: Int,
                                  private val method: String,
                                  private val payload: String? = null) : Step<HttpURLConnection>("CreateConnection") {
    override fun execute(): StepResult<HttpURLConnection> {
        val connection: HttpURLConnection = try {
            url.openConnection() as HttpURLConnection
        } catch (e: Exception) {
            return dealBreaker(
                "Error opening connection of url",
                "url" to url.toString(),
                "method" to method,
                "readTimeout" to readTimeout.toString()
            )
        }
        try {
            connection.readTimeout = readTimeout
            /*
                    Connect timeout should be very short, since it connects to a local endpoint.
                    We will postpone detailed investigation to a point where 100ms is too short
                 */
            connection.connectTimeout = 100

            /*
                    Avoid caching bugs in the client code, found some stack overflow issues that indicate that this might
                    happen.
                 */
            connection.useCaches = false
            connection.requestMethod = method
            if (payload != null) {
                connection.doOutput = true
                connection.outputStream.use { os ->
                    val input: ByteArray = payload.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
            }
            return success(connection)
        } catch (e: Exception) {
            return dealBreaker(
                "Error configuring connection",
                "url" to url.toString(),
                "method" to method,
                "readTimeout" to readTimeout.toString()
            )
        }
    }
}
