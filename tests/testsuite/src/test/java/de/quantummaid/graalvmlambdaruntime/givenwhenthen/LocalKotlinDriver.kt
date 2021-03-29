package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime
import de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntimeSpecs
import de.quantummaid.graalvmlambdaruntime.LambdaHandler
import de.quantummaid.graalvmlambdaruntime.LocalLambdaRuntimeConfiguration
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class LocalKotlinGraalVmLambdaRuntimeSpecs : GraalVmLambdaRuntimeSpecs {
    override fun testEnvironment() = TestEnvironment(LocalTestDriver(KotlinLambdaRuntimeExecutor()))
}

class LocalGraalVmGraalVmLambdaRuntimeSpecs : GraalVmLambdaRuntimeSpecs {
    override fun testEnvironment() = TestEnvironment(LocalTestDriver(GraalVmLambdaRuntimeExecutor()))
}

interface LambdaRuntimeExecutor {
    fun execute(port: Int)
}

class KotlinLambdaRuntimeExecutor : LambdaRuntimeExecutor {
    override fun execute(port: Int) {
        val graalVmLambdaRuntime = GraalVmLambdaRuntime(LocalLambdaRuntimeConfiguration(port))
        Thread { graalVmLambdaRuntime.start(LambdaHandler(::handle)) }.start()
    }
}

class GraalVmLambdaRuntimeExecutor : LambdaRuntimeExecutor {
    override fun execute(port: Int) {
        Thread {
            runCommand(mapOf(), listOf(
                    "$PROJECT_BASE_PATH/tests/testlambda/target/bootstrap",
                    port.toString()
            ))
        }.start()
    }
}

class LocalTestDriver(val executor: LambdaRuntimeExecutor) : TestDriver {
    override fun deploy(): Deployment {
        val port = FreePortPool.freePort()
        val lambdaEnvironment = LocalDeployment.startLocalLambdaEnvironment(port)
        executor.execute(port)
        return lambdaEnvironment
    }
}

class LocalDeployment(val requestQueue: BlockingQueue<String>,
                      val responseQueue: BlockingQueue<String>) : Deployment {

    override fun trigger(payload: String): String {
        requestQueue.put(payload)
        return responseQueue.take()
    }

    companion object {
        fun startLocalLambdaEnvironment(port: Int): Deployment {
            val requestQueue = LinkedBlockingQueue<String>() as BlockingQueue<String>
            val responseQueue = LinkedBlockingQueue<String>() as BlockingQueue<String>
            embeddedServer(Netty, port = port) {
                routing {
                    get("/2018-06-01/runtime/invocation/next") {
                        val next = requestQueue.take()
                        val requestId = UUID.randomUUID().toString()
                        call.response.header("Lambda-Runtime-Aws-Request-Id", requestId)
                        call.respondText(next, contentType = ContentType.parse("application/json"))
                    }
                    post("/2018-06-01/runtime/invocation/*/response") {
                        try {
                            val responsePayload: String = call.receiveText()
                            responseQueue.put(responsePayload)
                            call.respond(HttpStatusCode.OK)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }.start(wait = false)
            return LocalDeployment(requestQueue, responseQueue)
        }
    }
}