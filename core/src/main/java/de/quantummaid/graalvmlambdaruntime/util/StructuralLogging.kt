package de.quantummaid.graalvmlambdaruntime.util

import de.quantummaid.graalvmlambdaruntime.marshalling.MarshallerAndUnmarshaller
import mu.KLogger

class StructuralLogger(val marshaller: MarshallerAndUnmarshaller) {

    fun traceLogMap(baseLogger: KLogger, statement: () -> Map<String, *>) {
        baseLogger.trace { createLogStatement(statement) }
    }

    fun debugLogMap(baseLogger: KLogger, statement: () -> Map<String, *>) {
        baseLogger.debug { createLogStatement(statement) }
    }

    fun errorLogMap(baseLogger: KLogger, statement: () -> Map<String, *>) {
        baseLogger.error { createLogStatement(statement) }
    }

    private fun createLogStatement(statement: () -> Map<String, *>): String {
        val map = statement.invoke()
        return marshaller.marshal(map)
    }
}