package com.devlabs.devlabsbackend.individualscore.repository

import com.devlabs.devlabsbackend.individualscore.domain.IndividualScore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "individual_score")
interface IndividualScoreRepository : JpaRepository<IndividualScore, UUID> {
}