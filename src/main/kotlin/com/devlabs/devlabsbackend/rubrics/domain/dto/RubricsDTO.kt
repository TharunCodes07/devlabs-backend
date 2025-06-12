package com.devlabs.devlabsbackend.rubrics.domain.dto

import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import java.sql.Timestamp
import java.util.UUID

// Request DTOs
data class CreateRubricsRequest(
    val name: String,
    val userId: UUID, // The user creating the rubrics
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
    val userId: UUID, // The user updating the rubrics
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

// Response DTOs
data class RubricsResponse(
    val id: UUID,
    val name: String,
    val createdBy: CreatedByResponse,
    val createdAt: Timestamp,
    val criteria: List<CriterionResponse> = emptyList(),
    val isShared: Boolean = false
)

data class CreatedByResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val role: String
)
