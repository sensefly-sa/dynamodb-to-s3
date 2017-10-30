package io.sensefly.dynamodbtos3.writer

import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path


class LocalFileWriter constructor(root: Path, bucket: String, filePath: String) : Writer {

  private val log = LoggerFactory.getLogger(javaClass)

  private val writer: BufferedWriter

  init {
    val path = root.resolve(bucket).resolve(filePath)
    Files.createDirectories(path.parent)
    writer = BufferedWriter(FileWriter(path.toFile()))
    log.info("LocalFileWriter will write to {}", path.toAbsolutePath())
  }

  override fun write(item: String) {
    writer.write(item)
  }

  override fun close() {
    writer.close()
  }

}

class LocalFileWriterFactory(private val root: Path) : WriterFactory<LocalFileWriter> {

  override fun get(bucket: String, filePath: String): LocalFileWriter {
    return LocalFileWriter(root, bucket, filePath)
  }

}