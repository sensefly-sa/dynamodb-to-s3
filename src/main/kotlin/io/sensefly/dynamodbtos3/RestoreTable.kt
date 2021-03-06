package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Stopwatch
import com.google.common.util.concurrent.RateLimiter
import io.sensefly.dynamodbtos3.reader.ReaderFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.text.NumberFormat
import javax.inject.Inject


@Component
class RestoreTable @Inject constructor(
    private val dynamoDB: DynamoDB,
    private val amazonDynamoDB: AmazonDynamoDB,
    private val objectMapper: ObjectMapper,
    private val readerFactory: ReaderFactory<*>) {

  companion object {
    const val DEFAULT_WRITE_PERCENTAGE = 0.5
    private const val LOG_PROGRESS_FREQ = 5000
  }

  private val log = LoggerFactory.getLogger(javaClass)

  fun restore(source: URI, tableName: String, writePercentage: Double = DEFAULT_WRITE_PERCENTAGE) {

    val stopwatch = Stopwatch.createStarted()

    val writeCapacity = readCapacity(tableName)
    val batchSize = minOf(writeCapacity, 25)

    val permitsPerSec = writeCapacity.toDouble() * writePercentage * 10
    val rateLimiter = RateLimiter.create(permitsPerSec)

    log.info("Start {} restore from {} with {} rate ({} write capacity)", tableName, source, permitsPerSec, writeCapacity)

    val writeRequests = mutableListOf<WriteRequest>()
    var count = 0
    readerFactory.get(source).use { reader ->
      reader.read().bufferedReader().use {
        var line: String?
        do {
          line = it.readLine()
          if (line != null) {
            count++
            val item: Map<String, AttributeValue> = objectMapper.readValue(line)
            writeRequests.add(WriteRequest(PutRequest(item)))
            if (writeRequests.size == batchSize) {
              write(mapOf(tableName to writeRequests), rateLimiter)
              writeRequests.clear()
            }
          }
          if (count % LOG_PROGRESS_FREQ == 0) {
            log.info("Table {}: {} items written ({})", tableName, NumberFormat.getInstance().format(count), stopwatch)
          }
        } while (line != null)

        // insert remaining items
        if (writeRequests.isNotEmpty()) {
          write(mapOf(tableName to writeRequests), rateLimiter)
        }
      }
    }
    log.info("Restore {} table ({} items) completed in {}", tableName, NumberFormat.getInstance().format(count), stopwatch)
  }

  private fun write(writeRequests: Map<String, List<WriteRequest>>, rateLimiter: RateLimiter) {
    val request = BatchWriteItemRequest()
        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .withRequestItems(writeRequests)

    val result = amazonDynamoDB.batchWriteItem(request)
    val consumedCapacity = if (result.consumedCapacity == null) 100.0 else result.consumedCapacity[0].capacityUnits

    val consumed = Math.round(consumedCapacity * 10).toInt()
    val wait = rateLimiter.acquire(consumed)
    log.debug("consumed: {}, wait: {}, capacity: {}", consumed, wait, consumedCapacity)

    if (result.unprocessedItems.isNotEmpty()) {
      log.debug("Reprocess {} items", result.unprocessedItems.size)
      write(result.unprocessedItems, rateLimiter)
    }
  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.writeCapacityUnits)
  }

}