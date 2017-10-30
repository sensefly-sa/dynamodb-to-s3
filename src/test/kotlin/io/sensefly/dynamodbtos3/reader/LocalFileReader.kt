package io.sensefly.dynamodbtos3.reader

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.net.URL

class LocalFileReader(source: URL) : Reader {

  private val log = LoggerFactory.getLogger(javaClass)

  private var inputStream: FileInputStream? = null

  init {

    log.debug("{}; protocol: {}, path: {}", source, source.protocol, source.path)

    if (source.protocol != "file") {
      throw IllegalArgumentException("File source must be a local file URI (ie file://folder/my-table.json)")
    }

    val file = File(source.toURI())
    if (!file.exists()) {
      throw IllegalArgumentException("File $source does not exist")
    }

    inputStream = FileInputStream(file)
  }

  override fun read(): FileInputStream {
    return inputStream as FileInputStream
  }

  override fun close() {
    inputStream?.close()
  }

}

class LocalFileReaderFactory : ReaderFactory<LocalFileReader> {

  override fun get(source: URL): LocalFileReader {
    return LocalFileReader(source)
  }

}