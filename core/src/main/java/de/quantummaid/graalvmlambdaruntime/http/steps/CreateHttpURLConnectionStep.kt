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
