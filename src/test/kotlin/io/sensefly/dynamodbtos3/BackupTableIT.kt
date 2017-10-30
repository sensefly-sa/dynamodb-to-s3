package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.databind.ObjectMapper
import io.sensefly.dynamodbtos3.writer.LocalFileWriterFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.nio.ByteBuffer
import java.nio.file.Paths
import javax.inject.Inject

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class BackupTableIT {

  @Inject
  lateinit var dynamoDB: DynamoDB

  @Inject
  lateinit var amazonDynamoDB: AmazonDynamoDB

  @Inject
  lateinit var objectMapper: ObjectMapper


  @Before
  fun fillTable() {

    for (i in 0..5) {

      val byteArray = "hello world $i".toByteArray()

      val item = mutableMapOf<String, AttributeValue>()
      item.put("id", AttributeValue("$i"))
      item.put("string", AttributeValue("User $i"))
      item.put("boolean", AttributeValue().withBOOL(i % 2 == 0))
      item.put("null", AttributeValue().withNULL(true))
      item.put("string-set", AttributeValue().withSS("a", "b", "c"))
      item.put("number", AttributeValue().withN((i.toDouble() * 0.03).toString()))
      item.put("number-set", AttributeValue().withNS("0.1", "0.2", "0.3"))
      item.put("binary", AttributeValue().withB(ByteBuffer.wrap(byteArray)))
      item.put("binary-set", AttributeValue().withBS(ByteBuffer.wrap(byteArray)))
      item.put("map", AttributeValue().withM(mutableMapOf<String, AttributeValue>(
          "key1" to AttributeValue("value1"),
          "key2" to AttributeValue().withN("0.3696"),
          "key3" to AttributeValue().withBOOL(true))))
      item.put("empty-map", AttributeValue().withM(mutableMapOf<String, AttributeValue>()))
      item.put("attribute", AttributeValue().withL(AttributeValue("inner")))

      amazonDynamoDB.putItem("users", item)
    }

  }

  @Test
  fun backup() {
    val tableReader = BackupTable(dynamoDB, amazonDynamoDB, objectMapper, LocalFileWriterFactory(Paths.get("target")))
    tableReader.backup("users", "users-dump")
  }


}