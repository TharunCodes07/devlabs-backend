package com.devlabs.devlabsbackend.notification.domain
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant
import java.util.*

@Entity
class Notification (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    val title:String,
    val message: String,
    val isViewed: Boolean,

    val createdBy: UUID,
    val createdAt: Instant = Instant.now()
)