/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation

import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation.Poller.pollWithTimeout
import mu.KotlinLogging
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse
import software.amazon.awssdk.services.cloudformation.model.Stack
import software.amazon.awssdk.services.cloudformation.model.StackStatus
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors

private val log = KotlinLogging.logger { }

object CloudFormationWaiter {
    private const val MAX_NUMBER_OF_TRIES = 50
    private const val SLEEP_TIME_IN_MILLISECONDS = 5000
    fun waitForStackUpdate(stackIdentifier: String,
                           cloudFormationClient: CloudFormationClient) {
        waitForStatus(stackIdentifier, cloudFormationClient, StackStatus.UPDATE_COMPLETE)
    }

    fun waitForStackCreation(stackIdentifier: String,
                             cloudFormationClient: CloudFormationClient) {
        waitForStatus(stackIdentifier, cloudFormationClient, StackStatus.CREATE_COMPLETE)
    }

    fun waitForStackDeletion(stackIdentifier: String,
                             cloudFormationClient: CloudFormationClient) {
        waitFor(stackIdentifier, cloudFormationClient, Predicate<Optional<Stack>> { stack: Optional<Stack> ->
            stack
                    .map(Function { obj: Stack -> obj.stackStatus() })
                    .map<Boolean>(Function<StackStatus, Boolean> { stackStatus: StackStatus? ->
                        log.info("Waiting for stack {} to be deleted but it still exists in status {}",
                                stackIdentifier, stackStatus)
                        false
                    })
                    .orElse(true)
        })
    }

    private fun waitForStatus(stackIdentifier: String,
                              cloudFormationClient: CloudFormationClient,
                              expectedStatus: StackStatus) {
        waitFor(stackIdentifier, cloudFormationClient, Predicate<Optional<Stack>> { stack: Optional<Stack> ->
            stack
                    .map(Function { obj: Stack -> obj.stackStatus() })
                    .map<Boolean>(Function<StackStatus, Boolean> { stackStatus: StackStatus ->
                        val equals = expectedStatus == stackStatus
                        if (!equals) {
                            log.info("Waiting for stack {} to become {} but was {}",
                                    stackIdentifier, expectedStatus, stackStatus)
                        }
                        equals
                    })
                    .orElseGet(Supplier {
                        log.info("Did not find stack {}", stackIdentifier)
                        false
                    })
        }
        )
    }

    private fun waitFor(stackIdentifier: String,
                        cloudFormationClient: CloudFormationClient,
                        condition: Predicate<Optional<Stack>>) {
        pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS
        ) { conditionReached(stackIdentifier, cloudFormationClient, condition) }
    }

    private fun conditionReached(stackIdentifier: String,
                                 cloudFormationClient: CloudFormationClient,
                                 condidtion: Predicate<Optional<Stack>>): Boolean {
        val describeStacksResponse: DescribeStacksResponse = cloudFormationClient.describeStacks()
        val stacks: List<Stack> = describeStacksResponse.stacks().stream()
                .filter(Predicate { stack: Stack -> stackIdentifier == stack.stackName() })
                .collect(Collectors.toList())
        val stack: Optional<Stack>
        stack = if (stacks.isEmpty()) {
            Optional.empty()
        } else {
            Optional.of(stacks[0])
        }
        return condidtion.test(stack)
    }
}