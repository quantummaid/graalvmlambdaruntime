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

import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation.CloudFormationWaiter.waitForStackDeletion
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation.CloudFormationWaiter.waitForStackUpdate
import mu.KotlinLogging
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

private val log = KotlinLogging.logger { }

class CloudFormationHandler(val cloudFormationClient: CloudFormationClient) : AutoCloseable {
    fun createOrUpdateStack(stackName: String,
                            pathToTemplate: String,
                            stackParameters: Map<String, String>) {
        try {
            createStack(stackName, pathToTemplate, stackParameters)
        } catch (e: AlreadyExistsException) {
            log.info("Stack {} already exists, updating instead.", stackName)
            updateStack(stackName, pathToTemplate, stackParameters)
        }
    }

    fun createStack(stackIdentifier: String,
                    pathToTemplate: String,
                    stackParameters: Map<String, String>) {
        log.info("Creating stack {}...", stackIdentifier)
        val templateBody = fileToString(pathToTemplate)
        val createStackRequest: CreateStackRequest = CreateStackRequest.builder()
                .stackName(stackIdentifier)
                .capabilities(Capability.CAPABILITY_NAMED_IAM)
                .templateBody(templateBody)
                .parameters(stackParameters.entries
                        .stream()
                        .map { kv: Map.Entry<String, String> ->
                            Parameter.builder()
                                    .parameterKey(kv.key)
                                    .parameterValue(kv.value)
                                    .build()
                        }.collect(Collectors.toList()
                        ))
                .build()
        cloudFormationClient.createStack(createStackRequest)
        CloudFormationWaiter.waitForStackCreation(stackIdentifier, cloudFormationClient)
        log.info("Created stack {}.", stackIdentifier)
    }

    fun updateStack(stackIdentifier: String,
                    pathToTemplate: String,
                    stackParameters: Map<String, String>) {
        log.info("Updating stack {}...", stackIdentifier)
        val templateBody = fileToString(pathToTemplate)
        val updateStackRequest: UpdateStackRequest = UpdateStackRequest.builder()
                .stackName(stackIdentifier)
                .capabilities(Capability.CAPABILITY_NAMED_IAM)
                .templateBody(templateBody)
                .parameters(stackParameters.entries.stream().map { kv: Map.Entry<String?, String?> ->
                    Parameter.builder()
                            .parameterKey(kv.key)
                            .parameterValue(kv.value)
                            .build()
                }.collect(Collectors.toList()))
                .build()
        try {
            cloudFormationClient.updateStack(updateStackRequest)
        } catch (e: CloudFormationException) {
            val message: String = e.message!!
            if (message.contains("No updates are to be performed.")) {
                log.info("Stack {} was already up to date.", stackIdentifier)
                return
            } else {
                throw CloudFormationHandlerException(String.format("Exception thrown during update of stack %s", stackIdentifier), e)
            }
        }
        waitForStackUpdate(stackIdentifier, cloudFormationClient)
        log.info("Updated stack {}.", stackIdentifier)
    }

    fun deleteStacksStartingWith(stackPrefix: String?) {
        val listStacksResponse: ListStacksResponse = cloudFormationClient.listStacks()
        listStacksResponse.stackSummaries().stream()
                .filter(Predicate<StackSummary> { stack: StackSummary -> stack.stackStatus() == StackStatus.CREATE_COMPLETE || stack.stackStatus() == StackStatus.UPDATE_COMPLETE })
                .map<String>(Function<StackSummary, String> { obj: StackSummary -> obj.stackName() })
                .filter(Predicate { stackName: String -> stackName.startsWith(stackPrefix!!) })
                .forEach(Consumer { stackIdentifier: String -> deleteStack(stackIdentifier) })
    }

    private fun deleteStack(stackIdentifier: String) {
        log.info("Deleting stack {}...", stackIdentifier)
        val deleteStackRequest: DeleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackIdentifier)
                .build()
        cloudFormationClient.deleteStack(deleteStackRequest)
        waitForStackDeletion(stackIdentifier, cloudFormationClient)
        log.info("Deleted stack {}.", stackIdentifier)
    }

    override fun close() {
        cloudFormationClient.close()
    }

    companion object {
        fun connectToCloudFormation(): CloudFormationHandler {
            val cloudFormationClient: CloudFormationClient = CloudFormationClient.create()
            return CloudFormationHandler(cloudFormationClient)
        }

        private fun fileToString(filePath: String?): String {
            val contentBuilder = StringBuilder()
            try {
                Files.lines(Paths.get(filePath), StandardCharsets.UTF_8).use { stream -> stream.forEach(Consumer { s: String? -> contentBuilder.append(s).append("\n") }) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return contentBuilder.toString()
        }
    }
}