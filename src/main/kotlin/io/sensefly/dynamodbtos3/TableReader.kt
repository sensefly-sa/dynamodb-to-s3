package io.sensefly.dynamodbtos3

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


  fun backup(tableName: String, bucket: String, readPercentage: Double = DEFAULT_READ_PERCENTAGE, pattern: String = DEFAULT_PATTERN) {

    val stopwatch = Stopwatch.createStarted()

    val dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    val filePath = "$dir/$tableName.json"

    val consistentReadCapacity = readCapacity(tableName)
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
    var lastKey: Map<String, AttributeValue>?


    var count = 0
    S3Uploader(bucket, filePath, amazonS3).use {
      do {
        val scanResult = amazonDynamoDB.scan(scanRequest)

        val lines = scanResult.items.joinToString(System.lineSeparator()) { item -> objectMapper.writeValueAsString(item) }
        it.write(lines)

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
    log.info("Backup {} tables ({} items) completed in {}", tableName, NumberFormat.getInstance().format(count), stopwatch)

  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.readCapacityUnits)
  }

}
