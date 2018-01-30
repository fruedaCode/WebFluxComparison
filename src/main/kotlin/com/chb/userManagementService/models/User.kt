package com.chb.userManagementService.models

import com.chb.userManagementService.repositories.DynamoDbId
import java.time.Instant

data class User(@DynamoDbId var id: String = "",
                var firstName: String = "",
                var lastName: String = "",
                var age: Int = 0,
                var authenticated: Boolean = false,
                var authorities: List<String> = listOf(),
                var birthDate: Instant = Instant.ofEpochMilli(0))
