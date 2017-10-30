package io.sensefly.dynamodbtos3.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.nio.ByteBuffer

class ByteBufferSerializer : JsonSerializer<ByteBuffer>() {

  override fun serialize(value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider?) {
    gen.writeBinary(value.array())
  }

}