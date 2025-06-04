package com.devlabs.devlabsbackend.project.domain.DTO

import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.service.toUserResponse
import java.sql.Timestamp
import java.util.*

data class ProjectResponse(
    val id: UUID?,
    val title: String,
    val description: String,
    val objectives: String?,
    val githubUrl: String?,
    val status: ProjectStatus,
    val teamId: UUID?,
    val teamName: String?,
    val teamMembers: List<UserResponse>,
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
        githubUrl = this.githubUrl,
        status = this.status,
        teamId = this.team.id,
        teamName = this.team.name,
        teamMembers = this.team.members.map { it.toUserResponse() },
        courseId = this.course?.id,
        courseName = this.course?.name,
        reviewCount = this.reviews.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
