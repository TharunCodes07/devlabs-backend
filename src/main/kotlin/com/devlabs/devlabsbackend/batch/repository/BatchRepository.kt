package com.devlabs.devlabsbackend.batch.repository

import com.devlabs.devlabsbackend.batch.domain.Batch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "batch")
interface BatchRepository : JpaRepository<Batch, UUID>{
}