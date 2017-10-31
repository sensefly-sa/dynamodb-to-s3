package io.sensefly.dynamodbtos3.jackson

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.gregwoodfill.assert.shouldEqualJson
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.nio.ByteBuffer
import java.util.*

object AttributeValueSerializationSpek : Spek({

  given("an ObjectMapper") {

    val module = SimpleModule()
    module.addSerializer(ByteBuffer::class.java, ByteBufferSerializer())
    module.addDeserializer(ByteBuffer::class.java, ByteBufferDeserializer())

    val mapper = ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.registerModule(module)
    mapper.addMixIn(AttributeValue::class.java, AttributeValueMixIn::class.java)


    on("empty AttributeValue") {
      val av = AttributeValue()
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson "{}"
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with string") {
      val av = AttributeValue().withS("hello")
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """ { "S" : "hello"} """
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with boolean") {
      val av = AttributeValue().withBOOL(true)
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "BOOL": true}"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with number") {
      val av = AttributeValue().withN("123.45")
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "N": "123.45"}"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with null") {
      val av = AttributeValue().withNULL(true)
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "NULL": true}"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with string set") {
      val av = AttributeValue().withSS("foo", "bar")
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "SS": ["foo", "bar"] }"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with number set") {
      val av = AttributeValue().withNS("1", "2", "123.45")
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "NS": ["1", "2", "123.45"] }"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with AttributeValue") {
      val av = AttributeValue().withL(AttributeValue().withBOOL(true))
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "L": [ {"BOOL":true} ] }"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with map") {
      val map = mapOf("boolean" to AttributeValue().withBOOL(true), "string" to AttributeValue().withS("hello"))
      val av = AttributeValue().withM(map)
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{"M": {"boolean": {"BOOL": true}, "string": {"S": "hello"}}}"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with binary") {
      val byteArray = "hello world".toByteArray()
      val av = AttributeValue().withB(ByteBuffer.wrap(byteArray))
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "B": "${Base64.getEncoder().encodeToString(byteArray)}" }"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

    on("AttributeValue with binary set") {
      val hello = "hello".toByteArray()
      val world = "world".toByteArray()
      val av = AttributeValue().withBS(ByteBuffer.wrap(hello), ByteBuffer.wrap(world))
      val json = mapper.writeValueAsString(av)
      it("should serialize") {
        json shouldEqualJson """{ "BS": ["${Base64.getEncoder().encodeToString(hello)}", "${Base64.getEncoder().encodeToString(world)}"] }"""
      }
      it("should deserialize") {
        mapper.readValue<AttributeValue>(json) shouldEqual av
      }
    }

  }
})