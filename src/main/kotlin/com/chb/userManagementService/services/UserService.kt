package com.chb.userManagementService.services

import com.chb.userManagementService.models.User
import reactor.core.publisher.Mono

interface UserService {

    fun save(user: User): Mono<User>
    fun saveBlock(user: User): User
    fun findOne(id: String): Mono<User>
    fun findOneBlock(id: String): User
}