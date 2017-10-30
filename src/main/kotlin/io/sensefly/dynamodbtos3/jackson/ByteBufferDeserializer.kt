package io.sensefly.dynamodbtos3.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.nio.ByteBuffer


class ByteBufferDeserializer : JsonDeserializer<ByteBuffer>() {
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): ByteBuffer {
    return ByteBuffer.wrap(jp.binaryValue)
  }
}