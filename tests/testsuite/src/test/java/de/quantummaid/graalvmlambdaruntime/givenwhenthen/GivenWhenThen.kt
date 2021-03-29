package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import kotlin.test.assertEquals



interface TestDriver {
    fun deploy(): Deployment
}

interface Deployment {
    fun trigger(payload: String): String
}

class TestEnvironment(private val driver: TestDriver) {

    fun givenALambda(): Given {
        val deployment = driver.deploy()
        val testData = TestData(driver, deployment, null)
        return Given(testData)
    }
}

data class TestData(
        var driver: TestDriver,
        var deployment: Deployment,
        var response: String?)

class Given(private val testData: TestData) {
    fun andWhen(): When {
        return When(testData)
    }
}

class When(private val testData: TestData) {

    fun theLambdaFunctionIsInvoked(): Then {
        testData.response = testData.deployment.trigger("""{ "firstName" : "foo", "lastName" : "bar" }""")
        return Then(testData)
    }
}

class Then(private val testData: TestData) {

    fun theResponseWas(expected: String) {
        assertEquals(expected, testData.response)
    }
}