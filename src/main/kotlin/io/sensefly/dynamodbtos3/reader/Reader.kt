package io.sensefly.dynamodbtos3.reader

import java.io.InputStream
import java.net.URI


interface Reader : AutoCloseable {
  fun read(): InputStream
}

interface ReaderFactory<out T : Reader> {
  fun get(source: URI): T
}
