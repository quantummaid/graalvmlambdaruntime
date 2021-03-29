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
package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntimeSpecs
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cleanUp
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.deploy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import software.amazon.awssdk.core.SdkBytes.fromString
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType.REQUEST_RESPONSE
import kotlin.text.Charsets.UTF_8

class RemoteKotlinGraalVmLambdaRuntimeSpecs : GraalVmLambdaRuntimeSpecs {
    companion object {
        private var functionName: String? = null

        @BeforeAll
        @JvmStatic
        fun deployStacks() {
            functionName = deploy()
        }

        @AfterAll
        @JvmStatic
        fun cleanUpStacks() {
            cleanUp()
        }
    }

    override fun testEnvironment() = TestEnvironment(RemoteTestDriver(functionName!!))
}

class RemoteTestDriver(val functionName: String) : TestDriver {
    override fun deploy(): Deployment {
        return RemoteDeployment(functionName)
    }
}

class RemoteDeployment(val functionName: String) : Deployment {

    override fun trigger(payload: String): String {
        val client = LambdaClient.create()
        val response = client.invoke {
            it
                    .functionName(functionName)
                    .invocationType(REQUEST_RESPONSE)
                    .payload(fromString(payload, UTF_8))
        }
        return response.payload().asUtf8String()
    }
}
