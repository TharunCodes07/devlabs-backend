package com.devlabs.devlabsbackend.project.domain.DTO

import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.course.domain.Course
import java.sql.Timestamp
import java.util.*

data class ProjectResponse(
    val id: UUID?,
    val title: String,
    val description: String,
    val objectives: String?,
    val status: ProjectStatus,
    val teamId: UUID?,
    val teamName: String?,
    val courseId: UUID?,
    val courseName: String?,
    val reviewCount: Int,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)

fun Project.toProjectResponse(): ProjectResponse {
    return ProjectResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        objectives = this.objectives,
        status = this.status,
        teamId = this.team.id,
        teamName = this.team.name,
        courseId = this.course?.id,
        courseName = this.course?.name,
        reviewCount = this.reviews.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
