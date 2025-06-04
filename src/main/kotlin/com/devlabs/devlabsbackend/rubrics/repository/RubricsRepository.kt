package com.devlabs.devlabsbackend.rubrics.repository

import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "rubrics")
interface RubricsRepository: JpaRepository<Rubrics, UUID>{
}