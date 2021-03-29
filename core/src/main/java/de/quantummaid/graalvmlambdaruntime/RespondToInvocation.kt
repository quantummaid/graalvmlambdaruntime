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

import de.quantummaid.graalvmlambdaruntime.http.LambdaInvocationEndpointSdkClientUtils
import de.quantummaid.graalvmlambdaruntime.util.PerformanceMetrics
import de.quantummaid.graalvmlambdaruntime.util.StructuralLogger
import mu.KotlinLogging

private val baseLogger = KotlinLogging.logger { }

class RespondToInvocation {
    companion object {

        fun respond(invocation: Invocation,
                    configuration: LambdaRuntimeConfiguration,
                    metrics: PerformanceMetrics,
                    logger: StructuralLogger,
                    resultPayload: String?
        ) {
            metrics.addMetric("DeliverInvocationResult") {
                val url = configuration.responseUrl(invocation.lambdaRuntimeAwsRequestId)
                val result = LambdaInvocationEndpointSdkClientUtils.doHttpCall(
                        url,
                        "POST",
                        1000,
                        metrics,
                        logger,
                        resultPayload
                )
                logger.debugLogMap(baseLogger) {
                    mapOf(
                            "message" to "Delivered invocation result",
                            "lambdaRuntimeAwsRequestId" to invocation.lambdaRuntimeAwsRequestId,
                            "contentType" to invocation.contentType,
                            "lambdaEndpointResponseText" to result.normalResponseText,
                            "invocationPayload" to invocation.payload,
                            "resultPayload" to resultPayload
                    )
                }
            }
        }
    }
}
