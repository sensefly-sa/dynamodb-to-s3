package io.sensefly.dynamodbtos3

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class DynamodbToS3Application {

  private val log = LoggerFactory.getLogger(javaClass)

  @Value("\${table}")
  lateinit var table: String

  @Bean
  fun init(tableReader: TableReader) = CommandLineRunner {
    log.info("Backup table {}", table)
    tableReader.backup(table)
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(DynamodbToS3Application::class.java, *args)
}

