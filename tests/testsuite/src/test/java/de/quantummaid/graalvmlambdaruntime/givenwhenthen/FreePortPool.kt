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
package de.quantummaid.graalvmlambdaruntime.givenwhenthen

import java.util.concurrent.atomic.AtomicInteger
import de.quantummaid.graalvmlambdaruntime.givenwhenthen.FreePortPool
import java.net.ServerSocket
import java.io.IOException

object FreePortPool {
    private const val START_PORT = 9000
    private const val HIGHEST_PORT = 65535
    private val currentPort = AtomicInteger(START_PORT)

    fun freePort(): Int {
        val port = currentPort.incrementAndGet()
        return if (port >= HIGHEST_PORT) {
            currentPort.set(START_PORT)
            freePort()
        } else {
            try {
                val serverSocket = ServerSocket(port)
                serverSocket.close()
                port
            } catch (ex: IOException) {
                println("port $port in use, trying next one")
                freePort()
            }
        }
    }
}