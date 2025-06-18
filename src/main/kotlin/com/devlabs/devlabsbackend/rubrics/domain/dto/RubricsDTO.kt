package com.devlabs.devlabsbackend.rubrics.domain.dto

import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import java.sql.Timestamp
import java.util.*

data class CreateRubricsRequest(
    val name: String,
    val userId: String,
    val criteria: List<CreateCriterionRequest> = emptyList(),
    val isShared: Boolean = false
)

data class CreateCriterionRequest(
    val name: String,
    val description: String,
    val maxScore: Float,
    val isCommon: Boolean = false
)

data class UpdateRubricsRequest(
    val name: String? = null,
    val userId: String,
    val addCriteria: List<CreateCriterionRequest> = emptyList(),
    val updateCriteria: List<UpdateCriterionRequest> = emptyList(),
    val removeCriteriaIds: List<UUID> = emptyList()
)

data class UpdateCriterionRequest(
    val id: UUID,
    val name: String? = null,
    val description: String? = null,
    val maxScore: Float? = null,
    val isCommon: Boolean? = null
)

data class RubricsResponse(
    val id: UUID,
    val name: String,
    val createdBy: CreatedByResponse,
    val createdAt: Timestamp,
    val criteria: List<CriterionResponse> = emptyList(),
    val isShared: Boolean = false
)

data class CreatedByResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)
