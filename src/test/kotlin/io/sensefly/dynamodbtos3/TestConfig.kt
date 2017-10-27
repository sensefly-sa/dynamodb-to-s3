package io.sensefly.dynamodbtos3

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.apache.logging.log4j.util.Strings
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(Application::class)
class TestConfig {

  private val log = LoggerFactory.getLogger(javaClass)

  @Bean
  fun amazonDynamoDB(): AmazonDynamoDB {

    val env = Strings.trimToNull(System.getProperty("dynamodb.port"))
    val port = env ?: "8000"
    val endPoint = "http://localhost:" + port

    log.info("Configure local DynamoDB client on {}", endPoint)
    return AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endPoint, null))
        .build()
  }

}
