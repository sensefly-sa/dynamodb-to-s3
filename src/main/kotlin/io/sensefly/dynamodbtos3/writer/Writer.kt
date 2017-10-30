package io.sensefly.dynamodbtos3.writer

interface Writer : AutoCloseable {
  fun write(item: String)
}

interface WriterFactory<out T : Writer> {
  fun get(bucket: String, filePath: String): T
}
