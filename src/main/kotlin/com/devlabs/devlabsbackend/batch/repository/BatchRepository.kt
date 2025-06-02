package com.devlabs.devlabsbackend.batch.repository

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.batch.domain.dto.BatchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "batch")
interface BatchRepository : JpaRepository<Batch, UUID>{

    @Query("SELECT b FROM Batch b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR CAST(b.batch AS string) LIKE CONCAT('%', :query, '%')")
    fun findByNameOrYearContainingIgnoreCase(query: String): List<Batch>
}