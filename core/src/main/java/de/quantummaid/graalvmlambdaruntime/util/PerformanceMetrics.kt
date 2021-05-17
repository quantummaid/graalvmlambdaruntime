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
package de.quantummaid.graalvmlambdaruntime.util

import jdk.jfr.Enabled
import mu.KotlinLogging

private val baseLogger = KotlinLogging.logger { }

interface PerformanceMetrics : AutoCloseable {
    companion object {
        fun startMetering(topic: String, logger: StructuralLogger, enabled: Boolean): PerformanceMetrics {
            return if (enabled) {
                PerformanceMetricsImpl(topic, logger)
            } else {
                PerformanceMetricsNoop()
            }
        }
    }

    fun <T> addMetric(
            name: String,
            action: () -> T
    ): T

    fun results(): Map<String, Long>
}

private class PerformanceMetricsImpl(topic: String, val logger: StructuralLogger) : PerformanceMetrics {
    private val metrics = StopWatch.stopWatch(topic)
    val metricTopic = "MetricActiveTime[$topic]"

    init {
        metrics.start(metricTopic)
    }

    override fun <T> addMetric(
            name: String,
            action: () -> T
    ): T {
        return metrics.timedCall(name, action)
    }

    override fun results(): Map<String, Long> {
        metrics.stop(metricTopic)
        return metrics.timesMap()
    }

    override fun close() {
        logger.debugLogMap(baseLogger) { results() }
    }
}

private class PerformanceMetricsNoop : PerformanceMetrics {
    override fun <T> addMetric(
            name: String,
            action: () -> T
    ): T {
        return action()
    }

    override fun results(): Map<String, Long> {
        return mapOf()
    }

    override fun close() {
        // do nothing
    }
}
