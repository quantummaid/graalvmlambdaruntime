package de.quantummaid.graalvmlambdaruntime.util

import de.quantummaid.graalvmlambdaruntime.StopWatch
import mu.KotlinLogging

private val baseLogger = KotlinLogging.logger { }

interface PerformanceMetrics : AutoCloseable {
    companion object {
        private val enabled = "true" == System.getenv("DeveloperModeEnabled")
        fun startMetering(topic: String, logger: StructuralLogger): PerformanceMetrics {
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
    }
}
