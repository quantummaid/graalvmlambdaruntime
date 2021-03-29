package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime.Companion.startGraalVmLambdaRuntime
import java.net.URL

class LocalLambdaRuntimeConfiguration(val port: Int) : LambdaRuntimeConfiguration {
    private val environmentVariables = LambdaEnvironmentVariables()

    override fun nextInvocationUrl(): URL {
        return URL(
                "http://localhost:${port}/2018-06-01/runtime/invocation/next"
        )
    }

    override fun responseUrl(requestId: String): URL {
        return URL(
                "http://localhost:${port}/2018-06-01/runtime/invocation/${requestId}/response"
        )
    }

    override fun environmentVariables() = environmentVariables
}

fun main(args: Array<String>) {
    val configuration = if (args.isEmpty()) {
        RealLambdaRuntimeConfiguration()
    } else {
        val port = Integer.parseInt(args[0])
        LocalLambdaRuntimeConfiguration(port)
    }
    startGraalVmLambdaRuntime(configuration) {
        mapOf(
                "firstName" to "Hans",
                "lastName" to "Wurst"
        )
    }
}