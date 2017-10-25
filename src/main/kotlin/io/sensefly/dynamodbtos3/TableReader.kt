package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.inject.Inject


@Component
class TableReader @Inject constructor(private val dynamoDB: DynamoDB, private val amazonDynamoDB: AmazonDynamoDB) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun backup(tableName: String) {

    val readCapacity = readCapacity(tableName)

    log.info("Backup {} with {} read capacity", tableName, readCapacity)

    val scanRequest = ScanRequest()
        .withTableName(tableName)
        .withLimit(readCapacity)
        .withConsistentRead(false)


    val scan = amazonDynamoDB.scan(scanRequest)
    for (item: MutableMap<String, AttributeValue> in scan.items) {
      log.info(item.toString())
    }

  }

  private fun readCapacity(tableName: String): Int {
    return Math.toIntExact(dynamoDB.getTable(tableName).describe().provisionedThroughput.readCapacityUnits)
  }

}