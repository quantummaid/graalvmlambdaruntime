package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.*

data class CommandExecution(val exitCode: Int,
                            val stdout: String,
                            val stderr: String,
                            val duration: Duration)

fun runCommand(env: Map<String, String>, command: List<String?>?): CommandExecution {
    val envArray = env.entries
            .map { entry: Map.Entry<String, String> -> entry.key + "=" + entry.value }
            .toTypedArray()
    val commandLine = java.lang.String.join(" ", command)
    println(commandLine)
    return try {
        val begin = Instant.now()
        val process = Runtime.getRuntime().exec(commandLine, envArray, null)
        val exitCode = process.waitFor()
        val end = Instant.now()
        val duration = Duration.between(begin, end)
        val output = inputStreamToString(process.inputStream)
        val error = inputStreamToString(process.errorStream)
        val commandExecution = CommandExecution(exitCode, output, error, duration)
        if (exitCode != 0) {
            throw RuntimeException(commandExecution.toString())
        }
        commandExecution
    } catch (e: IOException) {
        throw RuntimeException(e)
    } catch (e: InterruptedException) {
        throw RuntimeException(e)
    }
}

fun inputStreamToString(inputStream: InputStream?): String {
    val scanner = Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A")
    return if (scanner.hasNext()) {
        scanner.next()
    } else ""
}