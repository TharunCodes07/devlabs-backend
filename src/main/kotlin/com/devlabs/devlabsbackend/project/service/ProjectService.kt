package com.devlabs.devlabsbackend.project.service

import com.devlabs.devlabsbackend.core.config.CacheConfig
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.project.domain.DTO.CreateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.ProjectResponse
import com.devlabs.devlabsbackend.project.domain.DTO.UpdateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.toProjectResponse
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val teamRepository: TeamRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {

    @CacheEvict(
        value = [CacheConfig.PROJECT_CACHE], 
        allEntries = true
    )
    fun createProject(projectData: CreateProjectRequest): Project {
        try {
            val team = teamRepository.findById(projectData.teamId).orElseThrow {
                NotFoundException("Team with id ${projectData.teamId} not found")
            }
            team.members.size

            val courses = if (projectData.courseIds.isNotEmpty()) {
                courseRepository.findAllById(projectData.courseIds).also { foundCourses ->
                    if (foundCourses.size != projectData.courseIds.size) {
                        throw NotFoundException("Some courses were not found")
                    }
                    foundCourses.forEach { it.students.size }
                }.toMutableSet()
            } else {
                mutableSetOf()
            }
            val project = Project(
                title = projectData.title,
                description = projectData.description,
                objectives = projectData.objectives,
                githubUrl = projectData.githubUrl,
                status = if (courses.isEmpty()) ProjectStatus.ONGOING else ProjectStatus.PROPOSED,
                team = team,
                courses = courses
            )
            return projectRepository.save(project)
        } catch (e: Exception) {
            println("Error creating project: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_team_' + #teamId + '_' + #page + '_' + #size"
    )
    fun getProjectsByTeam(teamId: UUID, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }

        team.members.size

        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByTeam(team, pageable)
        projectsPage.content.forEach { project ->
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }

            project.team.members.size
            project.reviews.size
        }

        return PaginatedResponse(
            data = projectsPage.content.map { it.toProjectResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = projectsPage.totalPages,
                total_count = projectsPage.totalElements.toInt()
            )
        )
    }

    @Cacheable(value = [CacheConfig.PROJECT_CACHE], key = "'project_' + #projectId")
    fun getProjectById(projectId: UUID): Project {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        project.team.members.size
        project.courses.forEach { course ->
            course.students.size
            course.instructors.size
        }
        project.reviews.size

        return project
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_course_' + #courseId + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortOrder"
    )
    fun getProjectsByCourse(
        courseId: UUID,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "title",
        sortOrder: String = "asc"
    ): PaginatedResponse<ProjectResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        course.students.size
        course.instructors.size

        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val projectsPage = projectRepository.findByCourse(course, pageable)

        projectsPage.content.forEach { project ->
            project.team.members.size
            project.courses.forEach { c ->
                c.students.size
                c.instructors.size
            }
            project.reviews.size
        }

        return PaginatedResponse(
            data = projectsPage.content.map { it.toProjectResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = projectsPage.totalPages,
                total_count = projectsPage.totalElements.toInt()
            )
        )
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    fun updateProject(projectId: UUID, updateData: UpdateProjectRequest, requesterId: String): Project {
        val project = getProjectById(projectId)

        project.team.members.size

        if (project.status !in listOf(ProjectStatus.PROPOSED, ProjectStatus.REJECTED)) {
            throw IllegalArgumentException("Project cannot be edited in current status: ${project.status}")
        }
        updateData.title?.let { project.title = it }
        updateData.description?.let { project.description = it }
        updateData.objectives?.let { project.objectives = it }
        updateData.githubUrl?.let { project.githubUrl = it }

        project.updatedAt = Timestamp.from(Instant.now())
        project.courses.forEach { course ->
            course.students.size
            course.instructors.size
        }

        return projectRepository.save(project)
    }


    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    fun deleteProject(projectId: UUID, userId: String): Boolean {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        val isTeamMember = project.team.members.any { it.id == userId }
        val isCourseInstructor = project.courses.any { course ->
            course.instructors.any { it.id == userId }
        }
        if (!isTeamMember && !isCourseInstructor) {
            throw IllegalArgumentException("You don't have permission to delete this project")
        }

        projectRepository.delete(project)
        return true
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_active_all'"
    )
    @Transactional
    fun getAllActiveProjects(): List<ProjectResponse> {
        return projectRepository.findByStatusIn(listOf(ProjectStatus.ONGOING, ProjectStatus.PROPOSED))
            .map { it.toProjectResponse() }
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_user_course_' + #userId + '_' + #courseId + '_' + #page + '_' + #size"
    )
    fun getProjectsForUserByCourse(
        userId: String,
        courseId: UUID,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<ProjectResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }

        val teams = teamRepository.findByMemberList(user)
        teams.forEach { it.members.size }

        val projects = teams.flatMap { team ->
            projectRepository.findByTeam(team)
        }.filter { project ->
            project.courses.contains(course)
        }

        projects.forEach { project ->
            project.courses.forEach { courseEntity ->
                courseEntity.students.size
                courseEntity.instructors.size
            }
            project.reviews.size
        }

        val total = projects.size
        val totalPages = (total + size - 1) / size
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, total)

        val pagedProjects = if (startIndex < total) {
            projects.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return PaginatedResponse(
            data = pagedProjects.map { it.toProjectResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = total
            )
        )
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_user_' + #userId + '_' + #page + '_' + #size"
    )
    fun getProjectsForUser(userId: String, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val teams = teamRepository.findByMemberList(user)

        teams.forEach { it.members.size }

        val projects = teams.flatMap { team ->
            projectRepository.findByTeam(team)
        }

        projects.forEach { project ->
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            project.reviews.size
        }

        val total = projects.size
        val totalPages = (total + size - 1) / size
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, total)

        val pagedProjects = if (startIndex < total) {
            projects.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return PaginatedResponse(
            data = pagedProjects.map { it.toProjectResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = total
            )
        )
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_search_' + #userId + '_' + #courseId + '_' + #query + '_' + #page + '_' + #size"
    )
    fun searchProjectsByCourseForUser(
        userId: String,
        courseId: UUID,
        query: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<ProjectResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }

        course.students.size
        course.instructors.size

        val hasAccess = when (user.role) {
            Role.ADMIN, Role.MANAGER -> true
            Role.FACULTY -> course.instructors.contains(user)
            Role.STUDENT -> course.students.contains(user)
            else -> false
        }

        if (!hasAccess) {
            throw IllegalArgumentException("User does not have access to this course")
        }

        val pageable: Pageable = PageRequest.of(page, size)

        val projectsPage = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                projectRepository.findByCourseAndTitleContainingIgnoreCase(course, query, pageable)
            }

            Role.FACULTY -> {
                projectRepository.findByCourseAndTitleContainingIgnoreCase(course, query, pageable)
            }

            Role.STUDENT -> {
                projectRepository.findByCourseAndTitleContainingIgnoreCaseAndTeamMembersContaining(
                    course,
                    query,
                    user,
                    pageable
                )
            }

            else -> {
                Page.empty(pageable)
            }
        }
        projectsPage.content.forEach { project ->
            project.team.members.size
            project.courses.forEach { c ->
                c.students.size
                c.instructors.size
            }
            project.reviews.size
        }

        return PaginatedResponse(
            data = projectsPage.content.map { it.toProjectResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = projectsPage.totalPages,
                total_count = projectsPage.totalElements.toInt()
            )
        )
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_active_semester_' + #semesterId"
    )
    @Transactional
    fun getActiveProjectsBySemester(semesterId: UUID): List<ProjectResponse> {
        return projectRepository.findActiveProjectsBySemester(semesterId)
            .map { it.toProjectResponse() }
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_active_batch_' + #batchId"
    )
    @Transactional
    fun getActiveProjectsByBatch(batchId: UUID): List<ProjectResponse> {
        return projectRepository.findActiveProjectsByBatch(batchId)
            .map { it.toProjectResponse() }
    }

    @Cacheable(
        value = [CacheConfig.PROJECT_CACHE], 
        key = "'projects_active_faculty_' + #facultyId"
    )
    fun getActiveProjectsByFaculty(facultyId: String): List<ProjectResponse> {
        val faculty =
            userRepository.findById(facultyId).orElseThrow { NotFoundException("Faculty with id $facultyId not found") }
        val projects = projectRepository.findActiveProjectsByFaculty(facultyId)
        return projects.map { it.toProjectResponse() }
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    @Transactional
    fun approveProject(projectId: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User not found")
        }
        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw IllegalArgumentException("U are not authorized to approve projects")
        }
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        if (project.status == ProjectStatus.COMPLETED) {
            throw IllegalArgumentException("Project cannot be approved in current status: ${project.status}")
        }
        project.status = ProjectStatus.ONGOING
        project.updatedAt = Timestamp.from(Instant.now())
        projectRepository.save(project)
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    @Transactional
    fun rejectProject(projectId: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User not found")
        }
        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw IllegalArgumentException("U are not authorized to reject projects")
        }
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        if (project.status == ProjectStatus.COMPLETED) {
            throw IllegalArgumentException("Project cannot be rejected in current status: ${project.status}")
        }
        project.status = ProjectStatus.REJECTED
        project.updatedAt = Timestamp.from(Instant.now())
        projectRepository.save(project)
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    @Transactional
    fun reProposeProject(projectId: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User not found")
        }
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        if (project.status != ProjectStatus.REJECTED) {
            throw IllegalArgumentException("Project cannot be re-proposed in current status: ${project.status}")
        }
        project.status = ProjectStatus.PROPOSED
        project.updatedAt = Timestamp.from(Instant.now())
        projectRepository.save(project)
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    @Transactional
    fun completeProject(projectId: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User not found")
        }
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        if (project.status != ProjectStatus.ONGOING) {
            throw IllegalArgumentException("Project cannot be completed in current status: ${project.status}")
        }
        project.status = ProjectStatus.COMPLETED
        project.updatedAt = Timestamp.from(Instant.now())
        projectRepository.save(project)
    }

    @CacheEvict(value = [CacheConfig.PROJECT_CACHE], allEntries = true)
    @Transactional
    fun revertProjectCompletion(projectId: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User not found")
        }
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        if (project.status != ProjectStatus.COMPLETED) {
            throw IllegalArgumentException("Project cannot be reverted in current status: ${project.status}")
        }
        project.status = ProjectStatus.ONGOING
        project.updatedAt = Timestamp.from(Instant.now())
        projectRepository.save(project)
    }

    private fun createSort(sortBy: String, sortOrder: String): Sort {
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(direction, sortBy)
    }
}