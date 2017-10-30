package io.sensefly.dynamodbtos3.jackson

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.ByteBuffer


interface AttributeValueMixIn {

  companion object {
    const val L = "L"
    const val M = "M"
    const val BS = "BS"
    const val NS = "NS"
    const val SS = "SS"
    const val BOOL = "BOOL"
    const val NULL = "NULL"
    const val B = "B"
    const val N = "N"
    const val S = "S"
  }

  @JsonProperty(S)
  fun getS(): String

  @JsonProperty(S)
  fun setS(s: String)

  @JsonProperty(N)
  fun getN(): String

  @JsonProperty(N)
  fun setN(n: String)

  @JsonProperty(B)
  fun getB(): ByteBuffer

  @JsonProperty(B)
  fun setB(b: ByteBuffer)

  @JsonProperty(NULL)
  fun isNULL(): Boolean?

  @JsonProperty(NULL)
  fun setNULL(nU: Boolean?)

  @JsonProperty(BOOL)
  fun getBOOL(): Boolean?

  @JsonProperty(BOOL)
  fun setBOOL(bO: Boolean?)

  @JsonProperty(SS)
  fun getSS(): List<String>

  @JsonProperty(SS)
  fun setSS(sS: List<String>)

  @JsonProperty(NS)
  fun getNS(): List<String>

  @JsonProperty(NS)
  fun setNS(nS: List<String>)

  @JsonProperty(BS)
  fun getBS(): List<String>

  @JsonProperty(BS)
  fun setBS(bS: List<String>)

  @JsonProperty(M)
  fun getM(): Map<String, AttributeValue>

  @JsonProperty(M)
  fun setM(`val`: Map<String, AttributeValue>)

  @JsonProperty(L)
  fun getL(): List<AttributeValue>

  @JsonProperty(L)
  fun setL(`val`: List<AttributeValue>)

}