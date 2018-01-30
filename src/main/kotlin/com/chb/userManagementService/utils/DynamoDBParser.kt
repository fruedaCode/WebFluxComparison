package com.chb.userManagementService.utils

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class DynamoDBParser {
    fun <T: Any> marshall(input: T): Map<String, AttributeValue> {
        val clazz = input::class
        //Filter and get only the members from the constructor
        return clazz.declaredMembers.filter{ member ->
            clazz.constructors.first().parameters.find { member.name == it.name } != null
        }.map {
            val value = it.call(input)
            val attrValue = toAttributeValue(value)
            mapOf(Pair(it.name, attrValue))
        }.reduce {
            acc, map -> acc.plus(map)
        }

    }

    fun <T: Any> unMarshall(input: Map<String, AttributeValue>, clazz: KClass<T>): T{
        return unMarshallInternal(InternalUtils.toSimpleMapValue<Any>(input), clazz)
    }

    private fun <T: Any> unMarshallInternal(input: Map<String, Any>, clazz: KClass<T>): T{
        val map = mutableMapOf<KParameter, Any>()
        input.forEach{ entryMap ->

            clazz.constructors.first().parameters.filter {
                it.name == entryMap.key
            }.map{ property ->
                val value = when (entryMap.value) {
                    is Map<*, *> -> {
                        try{
                            unMarshallInternal(entryMap.value as Map<String, Any>, property.type.jvmErasure)
                        }catch (e: Exception){
                            Any()
                        }
                    }
                    else -> entryMap.value
                }

                when {
                    //Set null if its null
                    value == null -> {
                     when{
                         property.type.isMarkedNullable -> map.put(property, value)
                         else -> map.put(property, property.type.jvmErasure.createInstance())
                     }
                    }
                    //Same type or same superclass
                    value::class == property.type.jvmErasure || property.type.jvmErasure.isSuperclassOf(value::class) -> map.put(property, value)
                    //Particular case with BigDecimals
                    value is BigDecimal && property.type.jvmErasure == Int::class -> map.put(property, value.toInt())
                    //Particular case with Instants
                    value is String && property.type.jvmErasure == Instant::class -> map.put(property, Instant.ofEpochMilli(value.toLong()))
                    //Default case create emtpy instance
                    else -> map.put(property, property.type.jvmErasure.createInstance())
                }
            }

        }

        return clazz.constructors.first().callBy(map)
    }

    fun toAttributeValue(value: Any?): AttributeValue {
        val result = AttributeValue()
        when (value) {
            null -> return result.withNULL(java.lang.Boolean.TRUE)
            is Boolean -> return result.withBOOL(value)
            is String -> {
                when {
                    value.isEmpty() -> result.withNULL(java.lang.Boolean.TRUE)
                    else -> result.withS(value)
                }
            }
            is BigDecimal -> return result.withN(value.toPlainString())
            is Number -> return result.withN(value.toString())
            is ByteArray -> return result.withB(ByteBuffer.wrap((value)))
            is Set<*> -> {
                // default to an empty string set if there is no element
                val set = value as Set<Any>
                if (set.isEmpty()) {
                    result.setSS(LinkedHashSet())
                    return result
                }
                val element = set.iterator().next()
                when (element){
                    is String -> {
                        result.setSS(ArrayList(value as Set<String>))
                    }
                    is Number -> {
                        val out = ArrayList<String>(set.size)
                        for (n in value as Set<Number>) {
                            val bd = InternalUtils.toBigDecimal(n)
                            out.add(bd.toPlainString())
                        }
                        result.setNS(out)
                    }
                    else -> throw UnsupportedOperationException("element type: " + element.javaClass)
                }
            }
            is List<*> -> {
                val out = ArrayList<AttributeValue>()
                for (v in value as List<Any>) {
                    out.add(toAttributeValue(v))
                }
                result.setL(out)
            }
            is Map<*, *> ->{
                val castedValue = value as Map<String, Any>
                if (castedValue.size > 0) {
                    for ((key, value1) in castedValue) {
                        result.addMEntry(key, toAttributeValue(value1))
                    }
                } else {    // empty map
                    result.m = LinkedHashMap()
                }
            }
            is Instant -> {
                result.withS(value.toEpochMilli().toString())
            }
            else -> {
                //TODO: Reflection to get members and do recursion
                val map = mutableMapOf<String, AttributeValue>()
                value::class.memberProperties.forEach {
                    map.put(it.name, toAttributeValue(it.getter.call(value)))
                }
                return AttributeValue().withM(map)
            }
        }
        return result
    }
}