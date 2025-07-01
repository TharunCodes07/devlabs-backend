package com.devlabs.devlabsbackend.rubrics.repository

import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import java.util.*

@RepositoryRestResource(path = "rubrics")
interface RubricsRepository: JpaRepository<Rubrics, UUID>{
    @RestResource(exported = false)
    fun findByCreatedBy(createdBy: User): List<Rubrics>
    
    @RestResource(path = "findByCreatedBy")
    fun findByCreatedBy(createdBy: User, pageable: Pageable): Page<Rubrics>
    @RestResource(exported = false)
    fun findByIsSharedTrue(): List<Rubrics>
    
    @RestResource(path = "findByIsSharedTrue")
    fun findByIsSharedTrue(pageable: Pageable): Page<Rubrics>
    
    @Query("SELECT r FROM Rubrics r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByNameContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Rubrics>
    
    @Query("SELECT r FROM Rubrics r WHERE r.createdBy = :createdBy AND LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCreatedByAndNameContainingIgnoreCase(@Param("createdBy") createdBy: User, @Param("query") query: String, pageable: Pageable): Page<Rubrics>
}