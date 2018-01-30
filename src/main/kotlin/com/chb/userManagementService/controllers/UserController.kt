package com.chb.userManagementService.controllers

import com.chb.userManagementService.models.User
import com.chb.userManagementService.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @GetMapping("/{userId}")
    fun getOne(@PathVariable userId: String): Mono<ResponseEntity<User>>{
        return userService.findOne(userId).map { ok(it) }
    }

    @GetMapping("/{userId}/block")
    fun getOneBlock(@PathVariable userId: String): ResponseEntity<User>{
        return ok(userService.findOneBlock(userId))
    }

    @PostMapping()
    fun save(@RequestBody user: User): Mono<ResponseEntity<User>> {

        return userService.save(user)
                .map { ok(it) }
    }

    @PostMapping("/block")
    fun saveBlock(@RequestBody user: User): ResponseEntity<User> {

        return ok(userService.saveBlock(user))
    }

}