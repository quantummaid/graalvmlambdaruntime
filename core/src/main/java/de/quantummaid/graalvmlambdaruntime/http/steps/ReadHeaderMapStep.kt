package de.quantummaid.graalvmlambdaruntime.http.steps

import de.quantummaid.graalvmlambdaruntime.http.connectionSettingsAsMap
import java.net.HttpURLConnection

/**
 * Reads the header fields. I've encountered a header with key null, so this method replaces
 * null with "null" (String). The only implementation I found catches and ignores IO Exceptions
 * silently, so it is of the utter most importance, to call this method AFTER calling readResponseCode(...).
 */
class ReadHeaderMapStep(private val connection: HttpURLConnection) : Step<Map<String, List<String>>>("ReadHeaderMap") {
    override fun execute(): StepResult<Map<String, List<String>>> {
        return try {
            val headerFields = connection.headerFields
            val nullKeyReplacedWithNullString = headerFields.mapKeys { if (it.key == null) "null" else it.key }
            success(nullKeyReplacedWithNullString)
        } catch (e: Exception) {
            error(
                "Unexpected error",
                "exceptionMessage" to (e.message ?: ""),
                "exception" to e.stackTraceToString(),
                *connection.connectionSettingsAsMap()
            )
        }
    }
}
