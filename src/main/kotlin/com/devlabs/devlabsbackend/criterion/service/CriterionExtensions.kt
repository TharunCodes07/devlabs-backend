package com.devlabs.devlabsbackend.criterion.service

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse

/**
 * Extension function to convert Criterion to CriterionResponse
 */
fun Criterion.toCriterionResponse(): CriterionResponse {
    return CriterionResponse(
        id = this.id!!,
        name = this.name,
        description = this.description,
        maxScore = this.maxScore,
        isCommon = this.isCommon
    )
}
