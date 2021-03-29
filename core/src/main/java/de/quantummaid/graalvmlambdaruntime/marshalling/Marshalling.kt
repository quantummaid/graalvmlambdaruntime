package de.quantummaid.graalvmlambdaruntime.marshalling

import de.quantummaid.mapmaid.minimaljson.MinimalJsonMarshallerAndUnmarshaller.minimalJsonMarshallerAndUnmarshaller

interface MarshallerAndUnmarshaller {

    fun unmarshal(payload: String): Map<String, Any?>

    fun marshal(map: Map<String, Any?>): String
}

class MapMaidMarshallerAndUnmarshaller(
        private val internal: de.quantummaid.mapmaid.builder.MarshallerAndUnmarshaller<String>) : MarshallerAndUnmarshaller {

    @Suppress("UNCHECKED_CAST")
    override fun unmarshal(payload: String): Map<String, Any?> {
        return internal.unmarshaller().unmarshal(payload) as Map<String, Any?>
    }

    override fun marshal(map: Map<String, Any?>): String {
        val normalizedMap = normalizeMap(map)
        return internal.marshaller().marshal(normalizedMap)
    }

    companion object {
        fun defaultMarshallerAndUnmarshaller(): MapMaidMarshallerAndUnmarshaller {
            val minimalJsonMarshallerAndUnmarshaller = minimalJsonMarshallerAndUnmarshaller()
            return MapMaidMarshallerAndUnmarshaller(minimalJsonMarshallerAndUnmarshaller)
        }
    }
}

private fun normalizeMap(map: Map<String, Any?>): Map<String, Any?> {
    return map
            .map { (key, value) ->
                val normalizedValue = when (value) {
                    is Int -> value.toLong()
                    is Map<*, *> -> normalizeMap(value as Map<String, Any?>)
                    else -> value
                }
                key to normalizedValue
            }
            .toMap()
}