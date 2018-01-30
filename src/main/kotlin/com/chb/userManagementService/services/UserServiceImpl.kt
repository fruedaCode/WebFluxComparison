package com.chb.userManagementService.services

import com.chb.userManagementService.models.User
import com.chb.userManagementService.repositories.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserServiceImpl(private val repository: UserRepository) : UserService {

    override fun save(user: User): Mono<User> {
        return repository.save(user)
    }

    override fun saveBlock(user: User): User {
        return repository.saveBlock(user)
    }

    override fun findOne(id: String): Mono<User>{
        return repository.findOne(id)
    }

    override fun findOneBlock(id: String): User {
        return repository.findOneBlock(id)
    }

}