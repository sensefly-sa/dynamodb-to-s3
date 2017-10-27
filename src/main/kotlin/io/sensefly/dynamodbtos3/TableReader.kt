package io.sensefly.dynamodbtos3

import alex.mojaki.s3upload.MultiPartOutputStream
import alex.mojaki.s3upload.StreamTransferManager
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Stopwatch
import com.google.common.util.concurrent.RateLimiter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Component
class TableReader @Inject constructor(
    private val dynamoDB: DynamoDB,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val amazonS3: AmazonS3,
    private val objectMapper: ObjectMapper) {

  companion object {
    const val DEFAULT_PATTERN = "yyyy/MM/dd"
    const val DEFAULT_READ_PERCENTAGE = 0.5
  }

  private val log = LoggerFactory.getLogger(javaClass)

  private val numUploadThreads = 2
  private val queueCapacity = 2
  private val partSize = 5


  fun backup(tableName: String, bucket: String, readPercentage: Double = DEFAULT_READ_PERCENTAGE, pattern: String = DEFAULT_PATTERN) {

    val stopwatch = Stopwatch.createStarted()

    val consistentReadCapacity = readCapacity(tableName)
    val dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    val filePath = "$dir/$tableName.json"
    var count = 0

    // non consistent capacity = consistent capacity * 2
    val limit = consistentReadCapacity * 2
    val permitsPerSec = consistentReadCapacity.toDouble() * readPercentage * 10
    val rateLimiter = RateLimiter.create(permitsPerSec)

    log.info("Start {} backup with {} limit and {} rate ({} read capacity) to {}/{}", tableName, limit, permitsPerSec,
        consistentReadCapacity, bucket, filePath)

    val scanRequest = ScanRequest()
        .withTableName(tableName)
        .withLimit(limit)
        .withConsistentRead(false)
        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)

    val streamManager = StreamTransferManager(bucket, filePath, amazonS3, 1, numUploadThreads, queueCapacity, partSize)
    try {
      streamManager.multiPartOutputStreams[0].use {

        var lastKey: Map<String, AttributeValue>?

        do {
          val scanResult = amazonDynamoDB.scan(scanRequest)

          writeItems(scanResult.items, it) // `it` comes from use() method
          val size = scanResult.items.size
          count += size

          lastKey = scanResult.lastEvaluatedKey
          scanRequest.exclusiveStartKey = lastKey

          val consumedCapacity = scanResult.consumedCapacity.capacityUnits
          val consumed = Math.round(consumedCapacity * 10).toInt()
          val wait = rateLimiter.acquire(consumed)

          log.debug("consumed: {}, wait: {}, capacity: {}, count: {}", consumed, wait, consumedCapacity, count)

        } while (lastKey != null)

      }
    } catch (e: Exception) {
      streamManager.abort(e) // aborts all uploads
      throw e
    } finally {
      streamManager.complete()
    }

    log.info("Backup {} tables ({} items) completed in {}", tableName, NumberFormat.getInstance().format(count), stopwatch)

  }

  private fun writeItems(items: List<Map<String, AttributeValue>>, outputStream: MultiPartOutputStream) {
    val lines = items.joinToString(System.lineSeparator()) { item -> objectMapper.writeValueAsString(item) }
    write(lines, outputStream)
  }

  /**
   * Writing data and potentially sending off a part
   */
  private fun write(item: String, outputStream: MultiPartOutputStream) {
    outputStream.write(item.toByteArray())
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
