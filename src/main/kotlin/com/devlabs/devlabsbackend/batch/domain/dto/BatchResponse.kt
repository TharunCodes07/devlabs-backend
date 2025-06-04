package com.devlabs.devlabsbackend.batch.domain.dto

import java.time.Year
import java.util.UUID

data class BatchResponse(
    val id: UUID?,
    val name: String,
    val graduationYear: Year,
    val section: String,
    val isActive: Boolean,
)
