package de.quantummaid.graalvmlambdaruntime.http.steps

import de.quantummaid.graalvmlambdaruntime.http.connectionSettingsAsMap
import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

class ReadStreamStep(private val connection: HttpURLConnection,
                     private val streamType: String,
                     private val streamExtractor: (HttpURLConnection) -> InputStream?) :
    Step<String?>("Read${streamType}Stream") {

    override fun execute(): StepResult<String?> {
        val stream: InputStream? = try {
            streamExtractor(connection)
        } catch (e: Exception) {
            return error(
                "Exception GETTING stream of connection",
                "exceptionMessage" to (e.message ?: ""),
                "exception" to e.stackTraceToString(),
                *connection.connectionSettingsAsMap()
            )
        }
        if (stream != null) {
            return try {
                val responseText = stream.bufferedReader(StandardCharsets.UTF_8).use {
                    it.readText()
                }
                success(responseText)
            } catch (e: Exception) {
                error(
                    "Exception READING ${streamType} stream of connection",
                    "exceptionMessage" to (e.message ?: ""),
                    "exception" to e.stackTraceToString(),
                    *connection.connectionSettingsAsMap()
                )
            }
        } else {
            return success(null)
        }
    }
}
