package com.devlabs.devlabsbackend.review.domain.DTO

import java.time.LocalDate
import java.util.*

data class CreateReviewRequest (
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rubricsId: UUID,
    val userId: UUID,

    val courseIds: List<UUID>?=null,
    val semesterIds: List<UUID>?=null,
    val batchIds: List<UUID>?=null,
    val projectIds: List<UUID>?=null,

    val sections: List<String>?=null,
    )

data class UpdateReviewRequest (
    val name: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val rubricsId: UUID? = null,
    val userId: UUID,

    val addCourseIds: List<UUID> = emptyList(),
    val removeCourseIds: List<UUID> = emptyList(),
    val addSemesterIds: List<UUID> = emptyList(),
    val removeSemesterIds: List<UUID> = emptyList(),
    val addBatchIds: List<UUID> = emptyList(),
    val removeBatchIds: List<UUID> = emptyList(),
    val addProjectIds: List<UUID> = emptyList(),
    val removeProjectIds: List<UUID> = emptyList(),
    val addSectionIds: List<UUID> = emptyList(),
    val removeSectionIds: List<UUID> = emptyList(),
)

data class ReviewResponse(
    val id: UUID,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val courses: List<CourseInfo>,
    val projects: List<ProjectInfo>,
    val sections: List<String>,
    val rubricsInfo: RubricInfo,
)

data class ProjectInfo(
    val id: UUID,
    val title : String,
    val teamName: String,
    val teamMembers: List<TeamMemberInfo>
)

data class TeamMemberInfo(
    val id: UUID,
    val name: String
)

data class RubricInfo(
    val id: UUID,
    val name: String,
    val criteria: List<CriteriaInfo>
)

data class CriteriaInfo(
    val id: UUID,
    val name: String,
    val description: String,
    val maxScore: Float,
    val isCommon: Boolean
)

data class CourseInfo(
    val id: UUID,
    val name: String,
    val code: String,
    val semesterInfo: SemesterInfo,
)

data class SemesterInfo(
    val id : UUID,
    val name: String,
    val year: Int,
    val isActive: Boolean,
)