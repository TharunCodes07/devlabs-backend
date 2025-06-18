package com.devlabs.devlabsbackend.notification.repository

import com.devlabs.devlabsbackend.notification.domain.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*


@RepositoryRestResource(path = "notification")
interface NotificationsRepository : JpaRepository<Notification, UUID>{
}