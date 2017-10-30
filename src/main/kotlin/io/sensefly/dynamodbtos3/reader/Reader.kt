package io.sensefly.dynamodbtos3.reader

import java.io.InputStream
import java.net.URL


interface Reader : AutoCloseable {
  fun read(): InputStream
}

interface ReaderFactory<out T : Reader> {
  fun get(source: URL): T
}
