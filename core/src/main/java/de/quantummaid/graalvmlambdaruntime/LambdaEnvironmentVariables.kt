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

/*
    _HANDLER – The handler location configured on the function.
    _X_AMZN_TRACE_ID – The X-Ray tracing header.
    AWS_REGION – The AWS Region where the Lambda function is executed.
    AWS_EXECUTION_ENV – The runtime identifier, prefixed by AWS_Lambda_—for example, AWS_Lambda_java8.
    AWS_LAMBDA_FUNCTION_NAME – The name of the function.
    AWS_LAMBDA_FUNCTION_MEMORY_SIZE – The amount of memory available to the function in MB.
    AWS_LAMBDA_FUNCTION_VERSION – The version of the function being executed.
    AWS_LAMBDA_LOG_GROUP_NAME, AWS_LAMBDA_LOG_STREAM_NAME – The name of the Amazon CloudWatch Logs group and stream for the function.
    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN – The access keys obtained from the function's execution role.
    AWS_LAMBDA_RUNTIME_API – (Custom runtime) The host and port of the runtime API.
    LAMBDA_TASK_ROOT – The path to your Lambda function code.
    LAMBDA_RUNTIME_DIR – The path to runtime libraries.
    TZ – The environment's time zone (UTC). The execution environment uses NTP to synchronize the system clock.
 */
class LambdaEnvironmentVariables {
    val environmentVariableMap = System.getenv()
    val _HANDLER = environmentVariableMap["_HANDLER"]
    val AWS_REGION = environmentVariableMap["AWS_REGION"]
    val AWS_EXECUTION_ENV = environmentVariableMap["AWS_EXECUTION_ENV"]
    val AWS_LAMBDA_FUNCTION_NAME = environmentVariableMap["AWS_LAMBDA_FUNCTION_NAME"]
    val AWS_LAMBDA_FUNCTION_MEMORY_SIZE = environmentVariableMap["AWS_LAMBDA_FUNCTION_MEMORY_SIZE"]
    val AWS_LAMBDA_FUNCTION_VERSION = environmentVariableMap["AWS_LAMBDA_FUNCTION_VERSION"]
    val AWS_LAMBDA_LOG_GROUP_NAME = environmentVariableMap["AWS_LAMBDA_LOG_GROUP_NAME"]
    val AWS_ACCESS_KEY_ID = environmentVariableMap["AWS_ACCESS_KEY_ID"]
    val AWS_SECRET_ACCESS_KEY = environmentVariableMap["AWS_SECRET_ACCESS_KEY"]
    val AWS_SESSION_TOKEN = environmentVariableMap["AWS_SESSION_TOKEN"]
    val AWS_LAMBDA_RUNTIME_API = environmentVariableMap["AWS_LAMBDA_RUNTIME_API"]
    val LAMBDA_TASK_ROOT = environmentVariableMap["LAMBDA_TASK_ROOT"]
    val LAMBDA_RUNTIME_DIR = environmentVariableMap["LAMBDA_RUNTIME_DIR"]
    val TZ = environmentVariableMap["TZ"]
}

