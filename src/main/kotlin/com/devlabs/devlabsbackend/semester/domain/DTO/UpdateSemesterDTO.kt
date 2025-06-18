package com.devlabs.devlabsbackend.semester.domain.DTO

import java.util.*

data class UpdateSemesterDTO(
    val id: UUID,
    val name: String? = null,
    val year: Int? = null,
    val isActive: Boolean? = null,
)

