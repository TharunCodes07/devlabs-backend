package com.devlabs.devlabsbackend.semester.repository

import com.devlabs.devlabsbackend.semester.domain.Semester
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

//collectionResourceRel = "semester"

@RepositoryRestResource(path = "semester")
interface SemesterRepository: JpaRepository<Semester, UUID> {
}