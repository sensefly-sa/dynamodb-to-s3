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
  private val s3MockPort = 54879

  @Bean(destroyMethod = "shutdown")
  fun embeddedAmazonDynamoDB(): AmazonDynamoDBLocal {
    log.debug("java.library.path: {}", System.getProperty("java.library.path"))
    return DynamoDBEmbedded.create()
  }

  @Bean
  fun amazonDynamoDB(dynamoDBLocal: AmazonDynamoDBLocal): AmazonDynamoDB {
    return dynamoDBLocal.amazonDynamoDB()
  }

  @Bean(destroyMethod = "stop")
  fun s3Mock(): S3Mock {
    val s3Mock = S3Mock.Builder()
        .withPort(s3MockPort)
        .withInMemoryBackend()
        .build()
    s3Mock.start()
    return s3Mock
  }

  @Bean
  fun amazonS3(s3Mock: S3Mock): AmazonS3 {
    val endPoint = "http://localhost:$s3MockPort"
    log.info("Configure S3 mock on {}", endPoint)

    return AmazonS3ClientBuilder.standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(EndpointConfiguration(endPoint, "eu-central-1"))
        .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
        .build()
  }

}
