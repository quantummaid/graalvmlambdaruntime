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
package de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws

import de.quantummaid.graalvmlambdaruntime.givenwhenthen.PROJECT_BASE_PATH
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cloudformation.CloudFormationHandler
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.s3.S3Handler
import java.io.File
import java.util.*

const val PREFIX = "lambdaruntimetests"

fun deploy(): String {
    CloudFormationHandler.connectToCloudFormation().use {
        val bucketStackName = PREFIX + UUID.randomUUID().toString()
        it.createStack(
                bucketStackName,
                "$PROJECT_BASE_PATH/tests/testsuite/cf-bucket.yml",
                mapOf(
                        "StackIdentifier" to bucketStackName
                )
        )
        val key = S3Handler.uploadToS3Bucket(bucketStackName, File("$PROJECT_BASE_PATH/tests/testlambda/target/function.zip"))

        val functionStackName = PREFIX + UUID.randomUUID().toString()
        it.createStack(
                functionStackName,
                "$PROJECT_BASE_PATH/tests/testsuite/cf-function.yml",
                mapOf(
                        "StackIdentifier" to functionStackName,
                        "Bucket" to bucketStackName,
                        "BucketKey" to key
                )
        )

        return functionStackName
    }
}

fun cleanUp() {
    S3Handler.emptyAllBucketsStartingWith(PREFIX)
    CloudFormationHandler.connectToCloudFormation().use {
        it.deleteStacksStartingWith(PREFIX)
    }
}

fun createStackIdentifier(): String {
    return PREFIX + UUID.randomUUID().toString().subSequence(0, 16)
}

fun main() {
    cleanUp()
    deploy()

    cleanUp()
}