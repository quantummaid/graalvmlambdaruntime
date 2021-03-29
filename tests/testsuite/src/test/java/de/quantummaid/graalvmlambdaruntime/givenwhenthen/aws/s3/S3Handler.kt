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
package de.quantummaid.graalvmlambdaruntime.givenwhenthen.aws.s3

import mu.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.ListObjectsResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI

private val log = KotlinLogging.logger { }

object S3Handler {
    fun uploadToS3Bucket(bucketName: String,
                         content: String): String {
        val key = keyFromContent(content)
        S3Client.create().use { s3Client ->
            log.info("Uploading to S3 object {}/{}...", bucketName, key)
            if (!fileNeedsUploading(bucketName, key, s3Client)) {
                log.info("S3 object with matching MD5 already present, skipping upload.")
            } else {
                log.info("S3 object not already present, uploading...")
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(), RequestBody.fromString(content))
                log.info("Uploaded to S3 object {}/{}.", bucketName, key)
            }
            return key
        }
    }

    fun uploadToS3Bucket(bucketName: String,
                         file: File): String {
        val key = keyFromFile(file)
        S3Client.create().use { s3Client ->
            log.info("Uploading {} to S3 object {}/{}...", file, bucketName, key)
            if (!fileNeedsUploading(bucketName, key, s3Client)) {
                log.info("S3 object with matching MD5 already present, skipping upload.")
            } else {
                log.info("S3 object not already present, uploading...")
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(), file.toPath())
                log.info("Uploaded {} to S3 object {}/{}.", file, bucketName, key)
            }
            return key
        }
    }

    private fun fileNeedsUploading(bucketName: String,
                                   key: String,
                                   s3Client: S3Client): Boolean {
        val objectsResponse: ListObjectsResponse = s3Client.listObjects(ListObjectsRequest.builder()
                .bucket(bucketName)
                .build())
        return objectsResponse.contents()
                .map { it.key() }
                .none { key == it }
    }

    private fun keyFromFile(file: File): String {
        val newContentMD5 = Md5Checksum.ofFile(file)
        return newContentMD5.value
    }

    private fun keyFromContent(content: String): String {
        val newContentMD5 = Md5Checksum.ofString(content)
        return newContentMD5.value
    }

    fun emptyAllBucketsStartingWith(prefix: String) {
        S3Client.create().use { s3Client ->
            s3Client.listBuckets().buckets()
                    .map { it.name() }
                    .filter { it.startsWith(prefix) }
                    .forEach { deleteAllObjectsInBucket(it) }
        }
    }

    fun deleteAllObjectsInBucket(bucketName: String) {
        S3Client.create().use { s3Client ->
            val listObjectsResponse: ListObjectsResponse = s3Client.listObjects(ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .build())
            listObjectsResponse.contents().forEach { s3Object ->
                val key: String = s3Object.key()
                deleteFromS3Bucket(bucketName, key, s3Client)
            }
        }
    }

    private fun deleteFromS3Bucket(bucketName: String,
                                   key: String,
                                   s3Client: S3Client) {
        log.info("Deleting S3 object {}/{}...", bucketName, key)
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build())
        log.info("Deleted S3 object {}/{}.", bucketName, key)
    }
}