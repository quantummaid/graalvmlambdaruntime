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
