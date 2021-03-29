package de.quantummaid.graalvmlambdaruntime

class LambdaEnvironmentTainted private constructor(message: String,
                                                   cause: Exception?,
                                                   vararg errorDetails: Pair<String, Any>)
    : Exception(message, cause) {
    val errorDetails: Map<String, Any> = mapOf(
        "message" to message,
        "stackTrace" to this.stackTraceToString(),
        *errorDetails
    )

    companion object {
        fun lambdaEnvironmentTainted(message: String,
                                     vararg errorDetails: Pair<String, Any>): LambdaEnvironmentTainted {
            return LambdaEnvironmentTainted(message, null, *errorDetails)
        }

        fun lambdaEnvironmentTaintedByException(message: String,
                                                cause: Exception?,
                                                vararg errorDetails: Pair<String, Any>): LambdaEnvironmentTainted {
            return LambdaEnvironmentTainted(message, cause, *errorDetails)
        }
    }
}
