package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.s3.AmazonS3
import io.sensefly.dynamodbtos3.config.TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class BackupRestoreTableIT {

  companion object {
    const val BUCKET = "test-bucket"
  }

  @Inject
  lateinit var amazonDynamoDB: AmazonDynamoDB

  @Inject
  lateinit var amazonS3: AmazonS3

  @Inject
  lateinit var backupTable: BackupTable

  @Inject
  lateinit var restoreTable: RestoreTable

  @Before
  fun createTables() {
    val existingTables = amazonDynamoDB.listTables().tableNames
    if (existingTables.contains("users-to-backup")) {
      amazonDynamoDB.deleteTable("users-to-backup")
    }
    if (existingTables.contains("users-to-restore")) {
      amazonDynamoDB.deleteTable("users-to-restore")
    }

    val request = CreateTableRequest()
        .withTableName("users-to-backup")
        .withKeySchema(KeySchemaElement("id", "HASH"))
        .withAttributeDefinitions(AttributeDefinition("id", "S"))
        .withProvisionedThroughput(ProvisionedThroughput().withReadCapacityUnits(10).withWriteCapacityUnits(10))
    amazonDynamoDB.createTable(request)

    request.tableName = "users-to-restore"
    amazonDynamoDB.createTable(request)
  }

  @Before
  fun fillTable() {

    for (i in 1..30) {

      val byteArray = "hello world $i".toByteArray()

      val item = mutableMapOf<String, AttributeValue>()
      item.put("id", AttributeValue("$i"))
      item.put("string", AttributeValue("User $i"))
      item.put("boolean", AttributeValue().withBOOL(i % 2 == 0))
      item.put("null", AttributeValue().withNULL(true))
      item.put("string-set", AttributeValue().withSS("a", "b", "c"))
      item.put("number", AttributeValue().withN("123.45"))
      item.put("number-set", AttributeValue().withNS("0.1", "0.2", "0.3"))
      item.put("binary", AttributeValue().withB(ByteBuffer.wrap(byteArray)))
      item.put("binary-set", AttributeValue().withBS(ByteBuffer.wrap(byteArray)))
      item.put("map", AttributeValue().withM(mutableMapOf<String, AttributeValue>(
          "key1" to AttributeValue("value1"),
          "key2" to AttributeValue().withN("0.3696"),
          "key3" to AttributeValue().withBOOL(true))))
      item.put("empty-map", AttributeValue().withM(mutableMapOf<String, AttributeValue>()))
      item.put("attribute", AttributeValue().withL(AttributeValue("inner")))

      amazonDynamoDB.putItem("users-to-backup", item)
    }

    assertThat(amazonDynamoDB.scan(ScanRequest().withTableName("users-to-backup")).items).hasSize(30)
  }

  @Before
  fun createBucket() {
    amazonS3.createBucket(BUCKET)
  }

  @Test
  fun backupAndRestore() {
    backupTable.backup("users-to-backup", BUCKET, readPercentage = 100.0)

    val dirs = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    restoreTable.restore(URI("s3://$BUCKET/$dirs/users-to-backup.json"), "users-to-restore", writePercentage = 100.0)

    val sourceItems = amazonDynamoDB.scan(ScanRequest().withTableName("users-to-backup")).items
        .sortedWith(compareBy({ it["id"]?.s }))
    val restoredItems = amazonDynamoDB.scan(ScanRequest().withTableName("users-to-restore")).items
        .sortedWith(compareBy({ it["id"]?.s }))

    assertThat(sourceItems).hasSameSizeAs(restoredItems)
    assertThat(sourceItems).containsExactlyElementsOf(restoredItems)
  }

}