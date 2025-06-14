package com.devlabs.devlabsbackend.individualscore.domain.DTO

import java.util.UUID

/**
 * Request that includes a user ID
 */
data class UserIdRequest(
    val userId: UUID
)
