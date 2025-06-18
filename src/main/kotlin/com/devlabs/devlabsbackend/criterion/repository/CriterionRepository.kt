package com.devlabs.devlabsbackend.criterion.repository

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "criterion")
interface CriterionRepository : JpaRepository<Criterion, UUID> {
    fun findAllByRubrics(rubrics: Rubrics): List<Criterion>
}
