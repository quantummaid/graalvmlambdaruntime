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

import kotlin.test.assertEquals

interface TestDriver {
    fun deploy(): Deployment
}

interface Deployment {
    fun trigger(payload: String): String
}

class TestEnvironment(private val driver: TestDriver) {

    fun givenALambda(): Given {
        val deployment = driver.deploy()
        val testData = TestData(driver, deployment, null)
        return Given(testData)
    }
}

data class TestData(
        var driver: TestDriver,
        var deployment: Deployment,
        var response: String?)

class Given(private val testData: TestData) {
    fun andWhen(): When {
        return When(testData)
    }
}

class When(private val testData: TestData) {

    fun theLambdaFunctionIsInvoked(): Then {
        testData.response = testData.deployment.trigger("""{ "firstName" : "foo", "lastName" : "bar" }""")
        return Then(testData)
    }
}

class Then(private val testData: TestData) {

    fun theResponseWas(expected: String) {
        assertEquals(expected, testData.response)
    }
}