package com.devlabs.devlabsbackend.department.domain.dto

import java.util.*

data class DepartmentResponse(
    val id: UUID? = null,
    val name: String,
    val batches: List<DepartmentBatchResponse> = emptyList()
)

data class SimpleDepartmentResponse(
    val id: UUID,
    val name: String
)