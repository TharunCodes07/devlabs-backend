package com.devlabs.devlabsbackend.rubrics.service

import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import com.devlabs.devlabsbackend.criterion.service.toCriterionResponse
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreatedByResponse
import com.devlabs.devlabsbackend.rubrics.domain.dto.RubricsResponse

/**
 * Extension function to convert Rubrics to RubricsResponse
 */
fun Rubrics.toRubricsResponse(): RubricsResponse {
    val criteriaResponses = this.criteria.map { criterion ->
        criterion.toCriterionResponse()
    }
    
    return RubricsResponse(
        id = this.id!!,
        name = this.name,
        createdBy = CreatedByResponse(
            id = this.createdBy.id!!,
            name = this.createdBy.name,
            email = this.createdBy.email,
            role = this.createdBy.role.name
        ),
        createdAt = this.createdAt,
        criteria = criteriaResponses,
        isShared = this.isShared
    )
}
