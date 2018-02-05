package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Stopwatch
import io.sensefly.dynamodbtos3.reader.ReadLimit
import io.sensefly.dynamodbtos3.reader.ReadLimitFactory
import io.sensefly.dynamodbtos3.writer.WriterFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.metrics.GaugeService
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit.NANOSECONDS
import javax.inject.Inject

@Component
class BackupTable @Inject constructor(
    private val readLimitFactory: ReadLimitFactory,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val objectMapper: ObjectMapper,
    private val writerFactory: WriterFactory<*>,
    private val metricRegistry: MetricRegistry,
    private val gaugeService: GaugeService) {

  companion object {
    const val DEFAULT_PATTERN = "yyyy/MM/dd"
    private const val LOG_PROGRESS_FREQ = 5000
  }

  private val log = LoggerFactory.getLogger(javaClass)

  fun backup(tableName: String, bucket: String, readPercentage: Double, pattern: String = DEFAULT_PATTERN) {
    backup(tableName, bucket, readLimitFactory.fromPercentage(tableName, readPercentage), pattern)
  }

  fun backup(tableName: String, bucket: String, readCapacity: Int, pattern: String = DEFAULT_PATTERN) {
    backup(tableName, bucket, readLimitFactory.fromCapacity(readCapacity), pattern)
  }

  private fun backup(tableName: String, bucket: String, readLimit: ReadLimit, pattern: String) {

    val stopwatch = Stopwatch.createStarted()

    val dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    val filePath = "$dir/$tableName.json"

    log.info("Start {} backup with {} limit to {}/{}", tableName, readLimit.limit, bucket, filePath)

    val scanRequest = ScanRequest()
        .withTableName(tableName)
        .withLimit(readLimit.limit)
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
        val wait = readLimit.rateLimiter.acquire(consumed)

        gaugeService.submit("$tableName-backup-items", count.toDouble())
        metricRegistry.timer("$tableName-backup-duration")
            .update(stopwatch.elapsed(NANOSECONDS), NANOSECONDS)
        log.debug("consumed: {}, wait: {}, capacity: {}, count: {}", consumed, wait, consumedCapacity, count)

      } while (scanResult.lastEvaluatedKey != null)
    }
    log.info("Backup {} table ({} items) completed in {}", tableName, NumberFormat.getInstance().format(count), stopwatch)
  }

}
