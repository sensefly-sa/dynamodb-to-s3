package io.sensefly.dynamodbtos3.reader

import io.sensefly.dynamodbtos3.AbstractTestWithDynamoDB
import io.sensefly.dynamodbtos3.config.TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class ReadLimitFactoryTest : AbstractTestWithDynamoDB() {

  @Inject
  lateinit var readLimitFactory: ReadLimitFactory

  @Test
  fun fromPercentage() {
    val readLimit = readLimitFactory.fromPercentage(TABLE_TO_BACKUP, 50.0)
    assertThat(readLimit.limit).isEqualTo(20)
  }

  @Test
  fun fromCapacity() {
    val readLimit = readLimitFactory.fromCapacity(200)
    assertThat(readLimit.limit).isEqualTo(200)
  }


}
