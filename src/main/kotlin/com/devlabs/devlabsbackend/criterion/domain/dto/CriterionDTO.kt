package com.devlabs.devlabsbackend.criterion.domain.dto

import java.util.*

// Response DTOs
data class CriterionResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Float,
    val isCommon: Boolean
)
