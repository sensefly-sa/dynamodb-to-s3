package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.nio.ByteBuffer
import java.util.*


class AttributeValueSerializer : StdSerializer<AttributeValue>(AttributeValue::class.java) {

  override fun serialize(value: AttributeValue, gen: JsonGenerator, provider: SerializerProvider) {

    fun write(s: String?, field: String) {
      if (s != null) gen.writeStringField(field, s)
    }

    fun write(col: Collection<String>?, field: String) {
      if (col != null) {
        gen.writeArrayFieldStart(field)
        col.forEach { gen.writeString(it) }
        gen.writeEndArray()
      }
    }

    fun write(b: Boolean?, field: String) {
      if (b != null) gen.writeBooleanField(field, b)
    }

    gen.writeStartObject()
    write(value.s, "S")
    write(value.n, "N")
    write(value.ss, "SS")
    write(value.ns, "NS")
    write(value.bool, "BOOL")
    write(value.`null`, "NULL")

    if (value.b != null) {
      gen.writeStringField("B", base64(value.b))
    }
    if (value.bs != null) {
      gen.writeArrayFieldStart("BS")
      value.bs.forEach { gen.writeString(base64(it)) }
      gen.writeEndArray()
    }
    if (value.m != null) {
      gen.writeObjectFieldStart("M")
      value.m.forEach { k, v -> gen.writeObjectField(k, v) }
      gen.writeEndObject()
    }
    if (value.l != null) {
      gen.writeArrayFieldStart("L")
      value.l.forEach { gen.writeObject(it) }
      gen.writeEndArray()
    }

    gen.writeEndObject()
  }


  private fun base64(byteBuffer: ByteBuffer): String {
    val array = ByteArray(byteBuffer.remaining())
    byteBuffer.get(array)
    return Base64.getEncoder().encodeToString(array)
  }

}