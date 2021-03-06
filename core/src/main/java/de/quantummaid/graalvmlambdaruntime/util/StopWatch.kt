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

internal interface StopWatch {
    fun start(topic: String)
    fun stop(topic: String)
    fun <T> timedCall(
        topic: String,
        call: () -> T
    ): T

    fun summary(): String

    fun timesMap(): Map<String, Long>

    companion object {
        fun stopWatch(stopWatchTopic: String = ""): StopWatch {
            return StopWatchImpl(stopWatchTopic)
        }

        fun noopStopWatch(): StopWatch {
            return NoopStopWatch
        }
    }
}

private object NoopStopWatch : StopWatch {
    override fun start(topic: String) {
        // do nothing
    }

    override fun stop(topic: String) {
        // do nothing
    }

    override fun <T> timedCall(
        topic: String,
        call: () -> T
    ): T {
        return call()
    }

    override fun summary(): String {
        return "NoopStopWatch is not recording"
    }

    override fun timesMap(): Map<String, Long> {
        return mapOf()
    }
}

private class StopWatchImpl(private val stopWatchTopic: String) : StopWatch {
    private val timers = mutableMapOf<String, TopicTime>()

    override fun start(topic: String) {
        val topicTime = TopicTime()
        topicTime.start()
        timers[topic] = topicTime
    }

    override fun stop(topic: String) {
        val topicTime = timers[topic]!!
        topicTime.finish()
    }

    override fun <T> timedCall(
        topic: String,
        call: () -> T
    ): T {
        start(topic)
        val result = call()
        stop(topic)
        return result
    }

    override fun summary(): String {
        val subTimes = timers
            .map {
                "${it.key}: ${it.value.duration()}ms"
            }
            .joinToString(";")
        val overallTime = sum()
        return "StopWatch $stopWatchTopic: $subTimes;;Overall: ${overallTime}ms"
    }

    override fun timesMap(): Map<String, Long> {
        return timers.mapValues {
            it.value.duration()
        }
    }

    private fun sum(): Long {
        return timers
            .map {
                it.value.duration()
            }
            .sum()
    }
}

private class TopicTime {
    private var currentRunStart: Long? = null
    private var currentRunStop: Long? = null

    fun start() {
        if (this.currentRunStart == null) {
            currentRunStart = System.currentTimeMillis()
        } else {
            throw IllegalStateException()
        }
    }

    fun finish() {
        if (this.currentRunStart == null || this.currentRunStop != null) {
            throw IllegalStateException()
        } else {
            currentRunStop = System.currentTimeMillis()
        }
    }

    fun duration(): Long {
        if (this.currentRunStart != null && this.currentRunStop != null) {
            return this.currentRunStop!! - this.currentRunStart!!
        } else {
            throw IllegalStateException()
        }
    }
}
