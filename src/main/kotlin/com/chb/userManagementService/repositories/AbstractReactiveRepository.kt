package com.chb.userManagementService.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.*
import com.chb.userManagementService.utils.DynamoDBParser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.InvalidParameterException
import java.util.*
import javax.annotation.PostConstruct
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Retention(AnnotationRetention.RUNTIME)
annotation class DynamoDbId

abstract class AbstractReactiveRepository<T: Any>(private var client: AmazonDynamoDBAsync){

    companion object {
        private const val READ_CAPACITY_UNITS = 5L
        private const val WRITE_CAPACITY_UNITS = 5L
    }

    protected abstract val clazz: KClass<T>
    private val dynamoDBParser = DynamoDBParser()
    private val tableName by lazy { clazz.simpleName }
    private var primaryKeyName = "id"


    //Create table if doesn't exists
    @PostConstruct
    fun initialSetup(){
        val listTablesResult = client.listTables()

        if (!listTablesResult.tableNames.contains(this.tableName)){
            createTable()
        }

    }

    private fun createTable(){
        val idMember = clazz.primaryConstructor!!.parameters.find { p -> p.annotations.find { it is DynamoDbId } != null}

        if(idMember != null){
            this.primaryKeyName = idMember.name!!
            val attributeDefinitions = ArrayList<AttributeDefinition>()
            attributeDefinitions.add(AttributeDefinition().withAttributeName(primaryKeyName).withAttributeType("S"))

            val keySchemaElements = ArrayList<KeySchemaElement>()
            keySchemaElements.add(KeySchemaElement().withAttributeName(primaryKeyName).withKeyType(KeyType.HASH))

            val request = CreateTableRequest()
                    .withTableName(this.tableName)
                    .withKeySchema(keySchemaElements)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(ProvisionedThroughput().withReadCapacityUnits(READ_CAPACITY_UNITS)
                            .withWriteCapacityUnits(WRITE_CAPACITY_UNITS))

            client.createTable(request)
        }else{
            throw InvalidParameterException("No member annotated with DynamoDbId")
        }
    }

    fun save(entity: T): Mono<T> {

        val request = PutItemRequest()
                .withItem(dynamoDBParser.marshall(entity))
                .withTableName(tableName)
                .withReturnConsumedCapacity("TOTAL")

        val response = client.putItemAsync(request)

        return Mono.create<T> {
            response.get()
            it.success(entity)
        }

    }

    fun saveBlock(entity: T): T {

        val request = PutItemRequest()
                .withItem(dynamoDBParser.marshall(entity))
                .withTableName(tableName)
                .withReturnConsumedCapacity("TOTAL")

        client.putItem(request)
        return entity

    }

    fun findAll(): Flux<T> {
        val request = ScanRequest()
                .withTableName(tableName)

        val response = client.scanAsync(request)

        return Flux.create<T> { sink ->
            val responseItem = response.get()

            responseItem.items.map {
                dynamoDBParser.unMarshall(it, clazz)
            }.forEach{
                sink.next(it)
            }

            sink.complete()
        }
    }

    fun findOne(id: String): Mono<T>{
        val request = QueryRequest()
                .withTableName(tableName)
                .withKeyConditionExpression("$primaryKeyName = :$primaryKeyName")
                .withExpressionAttributeValues(mapOf(Pair(":$primaryKeyName", AttributeValue(id))))

        val response = client.queryAsync(request)

        return Mono.create<T> { sink ->
            val responseItem = response.get()
            when {
                responseItem.items.isEmpty() -> sink.error(InvalidParameterException("Id $id not found in the database"))
                else -> sink.success(dynamoDBParser.unMarshall(responseItem.items.first(), clazz))
            }
        }
    }

    fun findOneBlock(id: String): T {
        val request = QueryRequest()
                .withTableName(tableName)
                .withKeyConditionExpression("$primaryKeyName = :$primaryKeyName")
                .withExpressionAttributeValues(mapOf(Pair(":$primaryKeyName", AttributeValue(id))))

        return dynamoDBParser.unMarshall(client.query(request).items.first(), clazz)
    }

    fun deleteOne(id: String): Mono<Boolean>{

        val request = DeleteItemRequest()
                .withTableName(tableName)
                .withKey(mapOf(Pair(primaryKeyName, AttributeValue(id))))

        val response = client.deleteItemAsync(request)

        return Mono.create{
            response.get()
            it.success(true)
        }
    }


}