package io.sensefly.dynamodbtos3

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class TableReaderTest {

  @Inject
  lateinit var tableReader: TableReader

  @Test
  fun backup() {
    tableReader.backup("users", "toto")
  }


}