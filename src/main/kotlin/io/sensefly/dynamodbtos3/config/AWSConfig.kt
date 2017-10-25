package io.sensefly.dynamodbtos3.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AWSConfig {

  @Bean
  fun awsCredentialsProvider(): AWSCredentialsProvider {
    return DefaultAWSCredentialsProviderChain()
  }
}

