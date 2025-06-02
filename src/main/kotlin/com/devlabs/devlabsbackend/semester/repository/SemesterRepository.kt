package com.devlabs.devlabsbackend.semester.repository

import com.devlabs.devlabsbackend.semester.domain.DTO.SemesterResponse
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

//collectionResourceRel = "semester"

@RepositoryRestResource(path = "semester")
interface SemesterRepository : JpaRepository<Semester, UUID> {

    @Query("SELECT s FROM Semester s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR CAST(s.year AS string) LIKE CONCAT('%', :query, '%')")
    fun findByNameOrYearContainingIgnoreCase(@Param("query") query: String): List<Semester>

}