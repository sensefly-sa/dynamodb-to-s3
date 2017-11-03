package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Stopwatch
import com.google.common.util.concurrent.RateLimiter
import io.sensefly.dynamodbtos3.writer.WriterFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Component
class BackupTable @Inject constructor(
    private val dynamoDB: DynamoDB,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val objectMapper: ObjectMapper,
    private val writerFactory: WriterFactory<*>) {

  companion object {
    const val DEFAULT_PATTERN = "yyyy/MM/dd"
    const val DEFAULT_READ_PERCENTAGE = 0.5
    private const val LOG_PROGRESS_FREQ = 5000
  }

  private val log = LoggerFactory.getLogger(javaClass)

  fun backup(tableName: String, bucket: String, readPercentage: Double = DEFAULT_READ_PERCENTAGE,
             pattern: String = DEFAULT_PATTERN) {

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

    var count = 0
    var lastPrint = LOG_PROGRESS_FREQ
    writerFactory.get(bucket, filePath).use { writer ->
      do {
        val scanResult = amazonDynamoDB.scan(scanRequest)
        if (scanResult.items.isNotEmpty()) {
          val lines = scanResult.items.joinToString(System.lineSeparator(), postfix = System.lineSeparator()) { item ->
            objectMapper.writeValueAsString(item)
          }
          writer.write(lines)
        }

        count += scanResult.items.size
        if (count > lastPrint) {
          log.info("[{}] {} items sent ({})", tableName, NumberFormat.getInstance().format(lastPrint), stopwatch)
          lastPrint += LOG_PROGRESS_FREQ
        }

        scanRequest.exclusiveStartKey = scanResult.lastEvaluatedKey

        // scanResult.consumedCapacity is null with LocalDynamoDB
        val consumedCapacity = if (scanResult.consumedCapacity == null) 100.0 else scanResult.consumedCapacity.capacityUnits
        val consumed = Math.round(consumedCapacity * 10).toInt()
        val wait = rateLimiter.acquire(consumed)

        log.debug("consumed: {}, wait: {}, capacity: {}, count: {}", consumed, wait, consumedCapacity, count)

      } while (scanResult.lastEvaluatedKey != null)
    }
    log.info("Backup {} table ({} items) completed in {}", tableName, NumberFormat.getInstance().format(count), stopwatch)
  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.readCapacityUnits)
  }

}
