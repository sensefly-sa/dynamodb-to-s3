package io.sensefly.dynamodbtos3

import io.sensefly.dynamodbtos3.config.TestConfig
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(classes = arrayOf(TestConfig::class))
class AbstractIT