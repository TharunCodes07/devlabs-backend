package com.devlabs.devlabsbackend.department.repository

import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "department")
interface DepartmentRepository: JpaRepository<Department, UUID> {    @Query("SELECT new com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse(b.id, b.name, b.graduationYear, b.section) FROM Batch b WHERE b.department.id = :deptId")
    fun findBatchesByDepartmentId(@Param("deptId") deptId: UUID): List<DepartmentBatchResponse>    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByNameContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Department>
    
    @Query("SELECT d.id FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findDepartmentIdsByNameContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<UUID>@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.batches")
    fun findAllWithBatches(): List<Department>

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.batches")
    fun findAllWithBatches(pageable: Pageable): Page<Department>

    @Query("SELECT d.id FROM Department d")
    fun findAllDepartmentIds(pageable: Pageable): Page<UUID>

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.batches WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByNameContainingIgnoreCaseWithBatches(@Param("query") query: String, pageable: Pageable): Page<Department>
    
    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.batches WHERE d.id = :departmentId")
    fun findByIdWithBatches(@Param("departmentId") departmentId: UUID): Department?

    override fun findAll(pageable: Pageable): Page<Department>
}

