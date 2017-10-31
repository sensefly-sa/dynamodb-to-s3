package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.sensefly.dynamodbtos3.config.TestConfig
import io.sensefly.dynamodbtos3.reader.LocalFileReaderFactory
import io.sensefly.dynamodbtos3.writer.LocalFileWriterFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// was not able to use Spek with Spring so back to good old Junit
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class BackupRestoreTableIT {

  @Inject
  lateinit var dynamoDB: DynamoDB

  @Inject
  lateinit var amazonDynamoDB: AmazonDynamoDB

  @Inject
  lateinit var objectMapper: ObjectMapper

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

  @Test
  fun backupAndRestore() {
    val writerFactory = LocalFileWriterFactory(Paths.get("target"))
    val backupTable = BackupTable(dynamoDB, amazonDynamoDB, objectMapper, writerFactory)
    backupTable.backup("users-to-backup", "users-dump", readPercentage = 100.0)

    val dumpFile = Paths.get("target")
        .resolve("users-dump")
        .resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
        .resolve("users-to-backup.json")
        .toUri()

    val restoreTable = RestoreTable(dynamoDB, amazonDynamoDB, objectMapper, LocalFileReaderFactory())
    restoreTable.restore(dumpFile, "users-to-restore", writePercentage = 100.0)

    val sourceItems = amazonDynamoDB.scan(ScanRequest().withTableName("users-to-backup")).items
        .sortedWith(compareBy({ it["id"]?.s }))
    val restoredItems = amazonDynamoDB.scan(ScanRequest().withTableName("users-to-restore")).items
        .sortedWith(compareBy({ it["id"]?.s }))

    assertThat(sourceItems).hasSameSizeAs(restoredItems)
    assertThat(sourceItems).containsExactlyElementsOf(restoredItems)
  }

}