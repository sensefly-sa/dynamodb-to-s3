package io.sensefly.dynamodbtos3.jackson

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.ByteBuffer
import java.util.*


class AttributeValueSerializationTest {

  private val mapper = ObjectMapper()

  init {
    val module = SimpleModule()
    module.addSerializer(ByteBuffer::class.java, ByteBufferSerializer())
    module.addDeserializer(ByteBuffer::class.java, ByteBufferDeserializer())

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.registerModule(module)
    mapper.addMixIn(AttributeValue::class.java, AttributeValueMixIn::class.java)
  }

  private fun test(av: AttributeValue, expectedJson: String) {
    val json = mapper.writeValueAsString(av)
    assertJsonEquals(expectedJson, json)

    val readValue = mapper.readValue<AttributeValue>(json)
    assertThat(readValue).isEqualTo(av)
  }

  @Test
  fun test_empty_attributeValue() {
    test(AttributeValue(), "{}")
  }

  @Test
  fun test_string() {
    test(AttributeValue().withS("hello"), """ { "S" : "hello"} """)
  }

  @Test
  fun test_boolean() {
    test(AttributeValue().withBOOL(true), """{ "BOOL": true}""")
  }

  @Test
  fun test_number() {
    test(AttributeValue().withN("123.45"), """{ "N": "123.45"}""")
  }

  @Test
  fun test_null() {
    test(AttributeValue().withNULL(true), """{ "NULL": true}""")
  }

  @Test
  fun test_string_set() {
    test(AttributeValue().withSS("foo", "bar"), """{ "SS": ["foo", "bar"] }""")
  }

  @Test
  fun test_number_set() {
    test(AttributeValue().withNS("1", "2", "123.45"), """{ "NS": ["1", "2", "123.45"] }""")
  }

  @Test
  fun test_attribute_value() {
    test(AttributeValue().withL(AttributeValue().withBOOL(true)), """{ "L": [ {"BOOL":true} ] }""")
  }

  @Test
  fun test_map() {
    val map = mapOf("boolean" to AttributeValue().withBOOL(true), "string" to AttributeValue().withS("hello"))
    test(AttributeValue().withM(map), """{"M": {"boolean": {"BOOL": true}, "string": {"S": "hello"}}}""")
  }

  @Test
  fun test_binary() {
    val byteArray = "hello world".toByteArray()
    test(AttributeValue().withB(ByteBuffer.wrap(byteArray)),
        """{ "B": "${Base64.getEncoder().encodeToString(byteArray)}" }""")
  }

  @Test
  fun test_binary_set() {
    val hello = "hello".toByteArray()
    val world = "world".toByteArray()
    test(AttributeValue().withBS(ByteBuffer.wrap(hello), ByteBuffer.wrap(world)),
        """{ "BS": ["${Base64.getEncoder().encodeToString(hello)}", "${Base64.getEncoder().encodeToString(world)}"] }""")
  }

}