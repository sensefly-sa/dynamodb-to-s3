package io.sensefly.dynamodbtos3.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal
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

  @Bean(destroyMethod = "shutdown")
  fun embeddedAmazonDynamoDB(): AmazonDynamoDBLocal {
    log.debug("java.library.path: {}", System.getProperty("java.library.path"))
    return DynamoDBEmbedded.create()
  }

  @Bean
  fun amazonDynamoDB(dynamoDBLocal: AmazonDynamoDBLocal): AmazonDynamoDB {
    return dynamoDBLocal.amazonDynamoDB()
  }

  @Bean
  fun amazonS3(): AmazonS3 {
    val port = 54879
    val endPoint = "http://localhost:$port"
    log.info("Configure S3 mock on {}", endPoint)

    S3Mock.Builder()
        .withPort(port)
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
