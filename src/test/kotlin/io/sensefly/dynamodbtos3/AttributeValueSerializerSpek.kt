package io.sensefly.dynamodbtos3

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.gregwoodfill.assert.shouldEqualJson
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.nio.ByteBuffer
import java.util.*

object AttributeValueSerializerSpek : Spek({

  given("a serializer") {

    val module = SimpleModule()
    module.addSerializer(AttributeValue::class.java, AttributeValueSerializer())

    val mapper = ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.registerModule(module)

    on("empty AttributeValue") {
      it("should serialize") {
        mapper.writeValueAsString(AttributeValue()) shouldEqual "{}"
      }
    }

    on("AttributeValue") {
      it("should serialize string") {
        val av = AttributeValue().withS("hello")
        mapper.writeValueAsString(av) shouldEqualJson """ { "S" : "hello"} """
//        mapper.readValue(mapper.writeValueAsBytes(av), AttributeValue::class.java) shouldEqual av
      }
      it("should serialize boolean") {
        val av = AttributeValue().withBOOL(true)
        mapper.writeValueAsString(av) shouldEqualJson """{ "BOOL": true}"""
      }
      it("should serialize number") {
        val av = AttributeValue().withN("123.45")
        mapper.writeValueAsString(av) shouldEqualJson """{ "N": "123.45"}"""
      }
      it("should serialize null") {
        val av = AttributeValue().withNULL(true)
        mapper.writeValueAsString(av) shouldEqualJson """{ "NULL": true}"""
      }
      it("should serialize string collection") {
        val av = AttributeValue().withSS("foo", "bar")
        mapper.writeValueAsString(av) shouldEqualJson """{ "SS": ["foo", "bar"] }"""
      }
      it("should serialize number collection") {
        val av = AttributeValue().withNS("1", "2", "123.45")
        mapper.writeValueAsString(av) shouldEqualJson """{ "NS": ["1", "2", "123.45"] }"""
      }
      it("should serialize AttributeValue") {
        val av = AttributeValue().withL(AttributeValue().withBOOL(true))
        mapper.writeValueAsString(av) shouldEqualJson """{ "L": [ {"BOOL":true} ] }"""
      }
      it("should serialize map") {
        val map = mapOf("boolean" to AttributeValue().withBOOL(true), "string" to AttributeValue().withS("hello"))
        val av = AttributeValue().withM(map)
        mapper.writeValueAsString(av) shouldEqualJson """{"M": {"boolean": {"BOOL": true}, "string": {"S": "hello"}}}"""
      }
      it("should serialize binary") {
        val byteArray = "hello world".toByteArray()
        val av = AttributeValue().withB(ByteBuffer.wrap(byteArray))
        mapper.writeValueAsString(av) shouldEqualJson """{ "B": "${Base64.getEncoder().encodeToString(byteArray)}" }"""
      }
      it("should serialize binary collection") {
        val hello = "hello".toByteArray()
        val world = "world".toByteArray()
        val av = AttributeValue().withBS(ByteBuffer.wrap(hello), ByteBuffer.wrap(world))
        mapper.writeValueAsString(av) shouldEqualJson
            """{ "BS": ["${Base64.getEncoder().encodeToString(hello)}", "${Base64.getEncoder().encodeToString(world)}"] }"""
      }
    }

  }
})