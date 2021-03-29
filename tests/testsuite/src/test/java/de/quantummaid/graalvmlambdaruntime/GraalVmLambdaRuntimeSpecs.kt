package de.quantummaid.graalvmlambdaruntime

import de.quantummaid.graalvmlambdaruntime.givenwhenthen.TestEnvironment
import org.junit.jupiter.api.Test

interface GraalVmLambdaRuntimeSpecs {

    fun testEnvironment(): TestEnvironment

    @Test
    fun lambdaRuntimeCanStart() {
        testEnvironment().givenALambda()
                .andWhen().theLambdaFunctionIsInvoked()
                .theResponseWas("""{"firstName":"Hans","lastName":"Wurst"}""")
    }
}
