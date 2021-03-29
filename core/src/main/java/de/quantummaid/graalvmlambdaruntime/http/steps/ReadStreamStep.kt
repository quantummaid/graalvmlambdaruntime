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
