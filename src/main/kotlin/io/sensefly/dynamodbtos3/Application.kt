package io.sensefly.dynamodbtos3

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
  val app = SpringApplication(Application::class.java)
  app.setBannerMode(Banner.Mode.OFF)
  app.run(*args)
}

