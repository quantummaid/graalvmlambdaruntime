package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntimeSpecs
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.cleanUp
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.deploy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import software.amazon.awssdk.core.SdkBytes.fromString
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType.REQUEST_RESPONSE
import kotlin.text.Charsets.UTF_8

class RemoteKotlinGraalVmLambdaRuntimeSpecs : GraalVmLambdaRuntimeSpecs {
    companion object {
        private var functionName: String? = null

        @BeforeAll
        @JvmStatic
        fun deployStacks() {
            functionName = deploy()
        }

        @AfterAll
        @JvmStatic
        fun cleanUpStacks() {
            cleanUp()
        }
    }

    override fun testEnvironment() = TestEnvironment(RemoteTestDriver(functionName!!))
}

class RemoteTestDriver(val functionName: String) : TestDriver {
    override fun deploy(): Deployment {
        return RemoteDeployment(functionName)
    }
}

class RemoteDeployment(val functionName: String) : Deployment {

    override fun trigger(payload: String): String {
        val client = LambdaClient.create()
        val response = client.invoke {
            it
                    .functionName(functionName)
                    .invocationType(REQUEST_RESPONSE)
                    .payload(fromString(payload, UTF_8))
        }
        return response.payload().asUtf8String()
    }
}
