package com.devlabs.devlabsbackend.user.service

import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
){
    fun getAllUsers(): List<User>{
        return userRepository.findAll()
    }

    fun createUser(user: User): User{
        return userRepository.save(user)
    }
}