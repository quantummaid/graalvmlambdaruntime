/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.s3

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern
import kotlin.experimental.and

/**
 * Source: https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 */
class Md5Checksum internal constructor(value: String) {
    val value: String

    companion object {
        const val MD5_STRING_PATTERN = "[0-9a-fA-F]+"
        private val VALID_MD5_STRING = Pattern.compile(MD5_STRING_PATTERN).asMatchPredicate()

        fun ofString(string: String): Md5Checksum {
            return try {
                val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
                messageDigest.update(string.toByteArray(StandardCharsets.UTF_8))
                val digest: ByteArray = messageDigest.digest()
                Md5Checksum(bytesToString(digest))
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException(e)
            }
        }

        @JvmStatic
        fun ofFile(file: File): Md5Checksum {
            val b = md5ChecksumBytesOf(file.absolutePath)
            return Md5Checksum(bytesToString(b))
        }

        private fun bytesToString(bytes: ByteArray): String {
            val result = StringBuilder()
            for (aByte in bytes) {
                result.append(Integer.toString((aByte and 0xff.toByte()) + 0x100, 16).substring(1))
            }
            return result.toString()
        }

        private fun md5ChecksumBytesOf(filename: String): ByteArray {
            try {
                FileInputStream(filename).use { fis ->
                    val buffer = ByteArray(1024)
                    val complete: MessageDigest = MessageDigest.getInstance("MD5")
                    var numRead: Int
                    do {
                        numRead = fis.read(buffer)
                        if (numRead > 0) {
                            complete.update(buffer, 0, numRead)
                        }
                    } while (numRead != -1)
                    return complete.digest()
                }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }
    }

    init {
        require(VALID_MD5_STRING.test(value)) { String.format("md5 checksum '%s' must match pattern '%s'", value, MD5_STRING_PATTERN) }
        this.value = value.toLowerCase()
    }
}