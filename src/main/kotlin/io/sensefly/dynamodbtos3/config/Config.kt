package io.sensefly.dynamodbtos3.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.sensefly.dynamodbtos3.jackson.AttributeValueMixIn
import io.sensefly.dynamodbtos3.jackson.ByteBufferDeserializer
import io.sensefly.dynamodbtos3.jackson.ByteBufferSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.ByteBuffer


@Configuration
class Config {

  @Bean
  fun awsCredentialsProvider(): AWSCredentialsProvider {
    return DefaultAWSCredentialsProviderChain()
  }

  @Bean
  fun amazonDynamoDB(credentialsProvider: AWSCredentialsProvider): AmazonDynamoDB {
    return AmazonDynamoDBClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .build()
  }

  @Bean
  fun dynamoDB(amazonDynamoDB: AmazonDynamoDB): DynamoDB {
    return DynamoDB(amazonDynamoDB)
  }

  @Bean
  fun amazonS3(credentialsProvider: AWSCredentialsProvider): AmazonS3 {
    return AmazonS3ClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .build()
  }

  @Bean
  fun objectMapper(): ObjectMapper {

    val module = SimpleModule()
    module.addSerializer(ByteBuffer::class.java, ByteBufferSerializer())
    module.addDeserializer(ByteBuffer::class.java, ByteBufferDeserializer())

    val mapper = ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.registerModule(module)
    mapper.addMixIn(AttributeValue::class.java, AttributeValueMixIn::class.java)

    return mapper
  }
}

