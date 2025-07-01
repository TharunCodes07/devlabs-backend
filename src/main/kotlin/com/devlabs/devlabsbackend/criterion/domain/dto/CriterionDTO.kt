package com.devlabs.devlabsbackend.criterion.domain.dto

import java.util.*

data class CriterionResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Float,
    val isCommon: Boolean
)
