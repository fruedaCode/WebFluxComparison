package com.chb.userManagementService.configurations

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class DynamoDbConfig {
    @Value("\${amazon.dynamodb.endpoint}")
    private lateinit var amazonDynamoDBEndpoint: String

    @Value("\${amazon.dynamodb.region}")
    private lateinit var amamzonDynamoDBRegion: String

    @Value("\${amazon.dynamodb.accessKey}")
    private lateinit var accessKey: String

    @Value("\${amazon.dynamodb.secretKey}")
    private lateinit var secretKey: String

    @Bean
    fun amazonDynamoDBAsync(): AmazonDynamoDBAsync {
        return AmazonDynamoDBAsyncClientBuilder.standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, amamzonDynamoDBRegion))
                .withCredentials(object: AWSCredentialsProvider {
                    override fun refresh(){}

                    override fun getCredentials(): AWSCredentials {
                        return BasicAWSCredentials(accessKey, secretKey)
                    }
                })
                .build()
    }
}

