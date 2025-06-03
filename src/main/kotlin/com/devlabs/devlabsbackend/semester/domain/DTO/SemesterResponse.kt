package com.devlabs.devlabsbackend.semester.domain.DTO

import java.util.UUID

data class SemesterResponse(
    val id: UUID,
    val name: String,
    val year: Int,
    val isActive: Boolean
)

