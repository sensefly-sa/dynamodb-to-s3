package io.sensefly.dynamodbtos3

import alex.mojaki.s3upload.StreamTransferManager
import com.amazonaws.services.s3.AmazonS3


class S3Uploader(bucket: String, filePath: String, amazonS3: AmazonS3) : AutoCloseable {

  private val numUploadThreads = 2
  private val queueCapacity = 2
  private val partSize = 5
  private var streamManager = StreamTransferManager(bucket, filePath, amazonS3, 1, numUploadThreads, queueCapacity, partSize)
  private var outputStream = streamManager.multiPartOutputStreams[0]


  /**
   * Writing data and potentially sending off a part
   */
  fun write(item: String) {
    try {
      outputStream.write(item.toByteArray())
      outputStream.checkSize()
    } catch (e: InterruptedException) {
//      throw RuntimeException(e)
    } catch (e: Exception) {
      streamManager.abort(e) // aborts all uploads
      throw e
    }
  }

  override fun close() {
    streamManager.complete()
  }

}