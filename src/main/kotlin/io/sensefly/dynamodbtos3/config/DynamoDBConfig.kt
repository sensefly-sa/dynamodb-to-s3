package io.sensefly.dynamodbtos3.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.inject.Inject

@Configuration
class DynamoDBConfig @Inject constructor(private val credentialsProvider: AWSCredentialsProvider) {

  @Bean
  fun amazonDynamoDB(): AmazonDynamoDB {
    return AmazonDynamoDBClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .build()
  }

  @Bean
  fun dynamoDBMapperConfig(): DynamoDBMapperConfig {
    return DynamoDBMapperConfig.Builder()
        .withConversionSchema(ConversionSchemas.V2)
        .build()
  }

  @Bean
  fun dynamoDB(amazonDynamoDB: AmazonDynamoDB): DynamoDB {
    return DynamoDB(amazonDynamoDB)
  }

}
