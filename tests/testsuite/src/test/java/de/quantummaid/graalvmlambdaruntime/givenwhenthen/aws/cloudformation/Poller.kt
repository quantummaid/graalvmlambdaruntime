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
package de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation

import java.lang.InterruptedException
import java.util.function.BooleanSupplier

object Poller {
    private const val NUMBER_OF_TRIES = 60 * 1000
    private const val SLEEP_TIME = 1
    fun pollWithTimeout(condition: BooleanSupplier): Boolean {
        return pollWithTimeout(NUMBER_OF_TRIES, SLEEP_TIME, condition)
    }

    fun pollWithTimeout(maxNumberOfTries: Int,
                        sleepTimeInMilliseconds: Int,
                        condition: BooleanSupplier): Boolean {
        for (i in 0 until maxNumberOfTries) {
            val conditionHasBeenFullfilled = condition.asBoolean
            if (conditionHasBeenFullfilled) {
                return true
            }
            sleep(sleepTimeInMilliseconds)
        }
        return false
    }

    fun sleep(milliseconds: Int) {
        try {
            Thread.sleep(milliseconds.toLong())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}