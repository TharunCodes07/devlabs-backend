package com.devlabs.devlabsbackend.rubric.dto

import com.devlabs.devlabsbackend.rubric.domain.RubricItemType

data class CreateTemplateRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val items: List<CreateTemplateItemRequest>
)

data class CreateTemplateItemRequest(
    val criteriaName: String,
    val description: String? = null,
    val type: RubricItemType,
    val weight: Double = 1.0,
    val minScore: Double? = null,
    val maxScore: Double? = null,
    val isRequired: Boolean = true
)

data class UpdateTemplateRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null
)

data class CreateCustomRubricRequest(
    val items: List<CreateRubricItemRequest>
)

data class CreateRubricItemRequest(
    val criteriaName: String,
    val description: String? = null,
    val type: RubricItemType,
    val weight: Double = 1.0,
    val minScore: Double? = null,
    val maxScore: Double? = null
)
