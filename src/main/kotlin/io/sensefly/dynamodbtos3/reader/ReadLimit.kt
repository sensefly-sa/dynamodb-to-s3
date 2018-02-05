package io.sensefly.dynamodbtos3.reader

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.google.common.util.concurrent.RateLimiter
import org.springframework.stereotype.Component
import javax.inject.Inject

data class ReadLimit(val limit: Int, val rateLimiter: RateLimiter)

@Component
class ReadLimitFactory @Inject constructor(private val dynamoDB: DynamoDB) {

  fun fromPercentage(tableName: String, readPercentage: Double): ReadLimit {
    val consistentReadCapacity = readCapacity(tableName)
    // non consistent capacity = consistent capacity * 2
    val limit = consistentReadCapacity * 2
    val permitsPerSec = consistentReadCapacity.toDouble() * readPercentage * 10
    return ReadLimit(limit, RateLimiter.create(permitsPerSec))
  }

  fun fromCapacity(readCapacity: Int): ReadLimit {
    return ReadLimit(readCapacity, RateLimiter.create(readCapacity.toDouble()))
  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.readCapacityUnits)
  }

}