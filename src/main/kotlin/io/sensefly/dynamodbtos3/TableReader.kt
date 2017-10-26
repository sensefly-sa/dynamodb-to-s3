package io.sensefly.dynamodbtos3

import alex.mojaki.s3upload.MultiPartOutputStream
import alex.mojaki.s3upload.StreamTransferManager
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.s3.AmazonS3
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@Component
class TableReader @Inject constructor(
    private val dynamoDB: DynamoDB,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val amazonS3: AmazonS3) {

  private val log = LoggerFactory.getLogger(javaClass)

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
  private val numUploadThreads = 2
  private val queueCapacity = 2
  private val partSize = 5

  fun backup(tableName: String, bucket: String) {

    val readCapacity = readCapacity(tableName)
    val dir = LocalDateTime.now().format(dateFormatter)
    val filePath = "$dir/$tableName.json"

    log.info("Backup {} with {} read capacity to {}/{}", tableName, readCapacity, bucket, filePath)

    val scanRequest = ScanRequest()
        .withTableName(tableName)
        .withLimit(readCapacity)
        .withConsistentRead(false)

    val streamManager = StreamTransferManager(bucket, filePath, amazonS3, 1, numUploadThreads, queueCapacity, partSize)
    try {
      streamManager.multiPartOutputStreams[0].use {
        for (item: MutableMap<String, AttributeValue> in amazonDynamoDB.scan(scanRequest).items) {
          log.info(item.toString())
          write(item.toString(), it) // `it` comes from use() method
        }
      }
    } catch (e: Exception) {
      streamManager.abort(e) // aborts all uploads
      throw e
    } finally {
      streamManager.complete()
    }

  }

  // Writing data and potentially sending off a part
  private fun write(item: String, outputStream: MultiPartOutputStream) {
    outputStream.write((item + System.lineSeparator()).toByteArray())
    try {
      outputStream.checkSize()
    } catch (e: InterruptedException) {
      throw RuntimeException(e)
    }
  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.readCapacityUnits)
  }

}