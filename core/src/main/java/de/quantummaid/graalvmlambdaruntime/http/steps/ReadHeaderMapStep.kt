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
