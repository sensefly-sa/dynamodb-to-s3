package io.sensefly.dynamodbtos3.writer

import alex.mojaki.s3upload.StreamTransferManager
import com.amazonaws.services.s3.AmazonS3
import org.springframework.stereotype.Component
import javax.inject.Inject


class S3Writer constructor(bucket: String, filePath: String, amazonS3: AmazonS3) : Writer {

  private val numUploadThreads = 2
  private val queueCapacity = 2
  private val partSize = 5
  private var streamManager = StreamTransferManager(bucket, filePath, amazonS3, 1, numUploadThreads, queueCapacity, partSize)
  private var outputStream = streamManager.multiPartOutputStreams[0]

  /**
   * Writing data and potentially sending off a part
   */
  override fun write(item: String) {
    try {
      outputStream.write(item.toByteArray())
      outputStream.checkSize()
    } catch (e: InterruptedException) {
      throw RuntimeException(e)
    } catch (e: Exception) {
      streamManager.abort(e) // aborts all uploads
      throw e
    }
  }

  override fun close() {
    outputStream.close()
    streamManager.complete()
  }

}

@Component
class S3WriterFactory @Inject constructor(private val amazonS3: AmazonS3) : WriterFactory<S3Writer> {

  override fun get(bucket: String, filePath: String): S3Writer {
    return S3Writer(bucket, filePath, amazonS3)
  }

}
