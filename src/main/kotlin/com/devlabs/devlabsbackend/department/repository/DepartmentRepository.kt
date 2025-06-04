package com.devlabs.devlabsbackend.department.repository

import com.devlabs.devlabsbackend.department.domain.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "departments")
interface DepartmentRepository: JpaRepository<Department, UUID> {
}