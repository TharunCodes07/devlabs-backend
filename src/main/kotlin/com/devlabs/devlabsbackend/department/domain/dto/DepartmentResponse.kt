package com.devlabs.devlabsbackend.department.domain.dto

import java.util.UUID

data class DepartmentResponse(
    val id: UUID? = null,
    val name: String,
)
