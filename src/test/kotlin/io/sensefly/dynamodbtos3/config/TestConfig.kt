package io.sensefly.dynamodbtos3.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.findify.s3mock.S3Mock
import io.sensefly.dynamodbtos3.Application
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

    // During dev, LocalDynamoDB is running on port 8000
    val port = System.getProperty("dynamodb.port") ?: "8000"
    val endPoint = "http://localhost:" + port

    log.info("Configure local DynamoDB on {}", endPoint)
    return AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endPoint, null))
        .build()
  }

  @Bean
  fun amazonS3(): AmazonS3 {
    val port = System.getProperty("s3mock.port") ?: "8188"
    val endPoint = "http://localhost:$port"

    log.info("Configure S3 mock on {}", endPoint)

    S3Mock.Builder()
        .withPort(port.toInt())
        .withInMemoryBackend()
        .build()
        .start()
    return AmazonS3ClientBuilder.standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(EndpointConfiguration(endPoint, "eu-central-1"))
        .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
        .build()
  }


}
