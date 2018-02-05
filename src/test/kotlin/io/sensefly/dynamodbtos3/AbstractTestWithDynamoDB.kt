package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import org.junit.Before
import javax.inject.Inject

// To run with IntelliJ add VM arguments:
// `-Djava.library.path=build/runtimeTestLibraries`
abstract class AbstractTestWithDynamoDB {

  @Inject
  lateinit var amazonDynamoDB: AmazonDynamoDB

  @Before
  fun createTables() {
    val existingTables = amazonDynamoDB.listTables().tableNames
    if (existingTables.contains(TABLE_TO_BACKUP)) {
      amazonDynamoDB.deleteTable(TABLE_TO_BACKUP)
    }
    if (existingTables.contains(TABLE_TO_RESTORE)) {
      amazonDynamoDB.deleteTable(TABLE_TO_RESTORE)
    }

    val request = CreateTableRequest()
        .withTableName(TABLE_TO_BACKUP)
        .withKeySchema(KeySchemaElement("id", "HASH"))
        .withAttributeDefinitions(AttributeDefinition("id", "S"))
        .withProvisionedThroughput(
            ProvisionedThroughput().withReadCapacityUnits(10).withWriteCapacityUnits(10))
    amazonDynamoDB.createTable(request)

    request.tableName = TABLE_TO_RESTORE
    amazonDynamoDB.createTable(request)
  }

  companion object {
    const val TABLE_TO_BACKUP = "users-to-backup"
    const val TABLE_TO_RESTORE = "users-to-restore"
  }

}