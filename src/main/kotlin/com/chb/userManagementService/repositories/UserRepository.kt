package com.chb.userManagementService.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.chb.userManagementService.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository


@Repository
class UserRepository(@Autowired client: AmazonDynamoDBAsync) : AbstractReactiveRepository<User>(client) {

    override val clazz = User::class

}