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
