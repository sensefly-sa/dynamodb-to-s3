package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
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

// To run with IntelliJ add VM arguments:
// `-Djava.library.path=build/runtimeTestLibraries`
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = [(TestConfig::class)])
class BackupRestoreTableTest : AbstractTestWithDynamoDB() {

  companion object {
    const val BUCKET = "test-bucket"
  }

  @Inject
  lateinit var amazonS3: AmazonS3

  @Inject
  lateinit var backupTable: BackupTable

  @Inject
  lateinit var restoreTable: RestoreTable

  @Before
  fun fillTable() {

    for (i in 1..30) {

      val byteArray = "hello world $i".toByteArray()

      val item = mutableMapOf<String, AttributeValue>()
      item["id"] = AttributeValue("$i")
      item["string"] = AttributeValue("User $i")
      item["boolean"] = AttributeValue().withBOOL(i % 2 == 0)
      item["null"] = AttributeValue().withNULL(true)
      item["string-set"] = AttributeValue().withSS("a", "b", "c")
      item["number"] = AttributeValue().withN("123.45")
      item["number-set"] = AttributeValue().withNS("0.1", "0.2", "0.3")
      item["binary"] = AttributeValue().withB(ByteBuffer.wrap(byteArray))
      item["binary-set"] = AttributeValue().withBS(ByteBuffer.wrap(byteArray))
      item["map"] = AttributeValue().withM(mutableMapOf<String, AttributeValue>(
          "key1" to AttributeValue("value1"),
          "key2" to AttributeValue().withN("0.3696"),
          "key3" to AttributeValue().withBOOL(true)))
      item["empty-map"] = AttributeValue().withM(mutableMapOf<String, AttributeValue>())
      item["attribute"] = AttributeValue().withL(AttributeValue("inner"))

      amazonDynamoDB.putItem(TABLE_TO_BACKUP, item)
    }

    assertThat(amazonDynamoDB.scan(ScanRequest().withTableName(TABLE_TO_BACKUP)).items).hasSize(30)
  }

  @Before
  fun createBucket() {
    amazonS3.createBucket(BUCKET)
  }

  @Test
  fun backupAndRestore() {
    backupTable.backup(TABLE_TO_BACKUP, BUCKET, readPercentage = 100.0)

    val dirs = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    restoreTable.restore(URI("s3://$BUCKET/$dirs/users-to-backup.json"), TABLE_TO_RESTORE, writePercentage = 100.0)

    val sourceItems = amazonDynamoDB.scan(ScanRequest().withTableName(TABLE_TO_BACKUP)).items
        .sortedWith(compareBy({ it["id"]?.s }))
    val restoredItems = amazonDynamoDB.scan(ScanRequest().withTableName(TABLE_TO_RESTORE)).items
        .sortedWith(compareBy({ it["id"]?.s }))

    assertThat(sourceItems).hasSameSizeAs(restoredItems)
    assertThat(sourceItems).containsExactlyElementsOf(restoredItems)
  }

}