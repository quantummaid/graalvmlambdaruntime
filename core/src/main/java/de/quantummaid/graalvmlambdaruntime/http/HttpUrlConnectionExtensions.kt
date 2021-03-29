package de.quantummaid.graalvmlambdaruntime.http

import java.net.HttpURLConnection

fun HttpURLConnection.connectionSettingsAsMap(): Array<Pair<String, String>> {
    return arrayOf(
        "url" to url.toString(),
        "requestMethod" to requestMethod.toString(),
        "useCaches" to useCaches.toString(),
        "connectTimeout" to connectTimeout.toString(),
        "readTimeout" to readTimeout.toString(),
    )
}
