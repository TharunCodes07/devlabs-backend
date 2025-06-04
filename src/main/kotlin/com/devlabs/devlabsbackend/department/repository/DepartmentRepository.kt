package com.devlabs.devlabsbackend.department.repository

import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "departments")
interface DepartmentRepository: JpaRepository<Department, UUID> {

    @Query("SELECT new com.devlabs.devlabsbackend.department.domain.dto.DepartmentBatchResponse(b.id, b.name, b.graduationYear, b.section) FROM Batch b WHERE b.department.id = :deptId")
    fun findBatchesByDepartmentId(@Param("deptId") deptId: UUID): List<DepartmentBatchResponse>
}

