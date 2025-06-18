package com.devlabs.devlabsbackend.batch.repository

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import java.util.*

@RepositoryRestResource(path = "batch")
interface BatchRepository : JpaRepository<Batch, UUID>{

    @RestResource(exported = false)
    override fun <S : Batch> save(entity: S): S

    @RestResource(exported = false)
    override fun <S : Batch> saveAll(entities: MutableIterable<S>): MutableList<S>    @Query("SELECT b FROM Batch b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR CAST(b.graduationYear AS string) LIKE CONCAT('%', :query, '%')")
    fun findByNameOrYearContainingIgnoreCase(query: String): List<Batch>

    @Query("SELECT b FROM Batch b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR CAST(b.graduationYear AS string) LIKE CONCAT('%', :query, '%')")
    fun searchByNameOrYearContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Batch>    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Batch b WHERE :semester MEMBER OF b.semester")
    fun existsBySemester(@Param("semester") semester: com.devlabs.devlabsbackend.semester.domain.Semester): Boolean

    override fun findAll(pageable: Pageable): Page<Batch>

    fun findByStudentsContaining(student: User): List<Batch>

    fun findByIsActiveTrue(): List<Batch>
}