package com.devlabs.devlabsbackend.user.domain

import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

enum class Role {
    STUDENT,
    ADMIN,
    FACULTY,
    MANAGER
}

@Entity
@Table(name = "\"user\"")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    var email: String,
    var profileId: String,
    var password: String,
    var image: String? = null,
    var role: Role,
    var phoneNumber: String,
    var isActive: Boolean = true,
    var createdAt: Timestamp = Timestamp.from(Instant.now()),
    var lastPasswordChange: Timestamp? = null
)