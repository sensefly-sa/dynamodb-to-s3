package io.sensefly.dynamodbtos3.reader

import java.io.File
import java.io.FileInputStream
import java.net.URI

class LocalFileReader(source: URI) : Reader {

  private var inputStream: FileInputStream? = null

  init {
    if (source.scheme != "file") {
      throw IllegalArgumentException("File source must be a local file URI (ie file://folder/my-table.json)")
    }

    val file = File(source)
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

  override fun get(source: URI): LocalFileReader {
    return LocalFileReader(source)
  }

}