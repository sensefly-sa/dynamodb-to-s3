package io.sensefly.dynamodbtos3

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AWSConfig {

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
}

