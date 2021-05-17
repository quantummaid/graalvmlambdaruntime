/**
 * Copyright (c) 2021 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.marshalling.MapMaidMarshallerAndUnmarshaller.Companion.defaultMarshallerAndUnmarshaller
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging
import java.net.URL
import kotlin.system.exitProcess

private val baseLogger = KotlinLogging.logger { }

class GraalVmLambdaRuntime(private val configuration: LambdaRuntimeConfiguration) {

    fun start(enablePerformanceMetrics: Boolean, handler: LambdaHandler) {
        val environmentVariables = configuration.environmentVariables()
        val marshallerAndUnmarshaller = configuration.marshallerAndUnmarshaller()
        val logger = StructuralLogger(marshallerAndUnmarshaller)

        logger.debugLogMap(baseLogger) {
            mapOf(
                "Message" to "Main entered in environment",
                "Environment" to environmentVariables.environmentVariableMap
            )
        }

        val nextInvocationUrl = configuration.nextInvocationUrl()

        try {
            while (true) {
                PerformanceMetrics.startMetering("Lambda Invocation", logger, enablePerformanceMetrics)
                    .use { invocationMetrics ->
                        val nextInvocation = NextInvocation.nextInvocation(nextInvocationUrl, invocationMetrics, logger)
                        val event = nextInvocation.payload
                            ?.let { marshallerAndUnmarshaller.unmarshal(it) }
                            ?: emptyMap()
                        val response = handler.handle(event)
                        val responsePayload = marshallerAndUnmarshaller.marshal(response)
                        RespondToInvocation.respond(
                            nextInvocation,
                            configuration,
                            invocationMetrics,
                            logger,
                            responsePayload
                        )
                    }
            }
        } catch (e: LambdaEnvironmentTainted) {
            logger.errorLogMap(baseLogger) { e.errorDetails }
            exitProcess(1)
        }
        // No need to catch generic exceptions, the jvm is going to log it stderr and it'll end up in cloudwatch
    }

    companion object {
        @JvmStatic
        fun startGraalVmLambdaRuntime(lambdaHandler: LambdaHandler) {
            startGraalVmLambdaRuntime(false, lambdaHandler)
        }

        @JvmStatic
        fun startGraalVmLambdaRuntime(enablePerformanceMetrics: Boolean, lambdaHandler: LambdaHandler) {
            val configuration = RealLambdaRuntimeConfiguration()
            startGraalVmLambdaRuntime(configuration, enablePerformanceMetrics, lambdaHandler)
        }

        @JvmStatic
        fun startGraalVmLambdaRuntime(
            configuration: LambdaRuntimeConfiguration,
            enablePerformanceMetrics: Boolean,
            lambdaHandler: LambdaHandler
        ) {
            val graalVmLambdaRuntime = GraalVmLambdaRuntime(configuration)
            graalVmLambdaRuntime.start(enablePerformanceMetrics, lambdaHandler)
        }
    }
}

interface LambdaRuntimeConfiguration {
    fun nextInvocationUrl(): URL

    fun responseUrl(requestId: String): URL

    fun marshallerAndUnmarshaller() = defaultMarshallerAndUnmarshaller()

    fun environmentVariables(): LambdaEnvironmentVariables
}

class RealLambdaRuntimeConfiguration : LambdaRuntimeConfiguration {
    private val environmentVariables = LambdaEnvironmentVariables()

    private val nextInvocationUrl = URL(
        "http://${environmentVariables.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/next"
    )

    override fun nextInvocationUrl() = nextInvocationUrl

    override fun responseUrl(requestId: String) = URL(
        "http://${environmentVariables.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/${requestId}/response"
    )

    override fun environmentVariables() = environmentVariables
}

fun interface LambdaHandler {
    fun handle(event: Map<String, Any?>): Map<String, Any?>
}
