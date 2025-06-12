package com.devlabs.devlabsbackend.project.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.project.domain.DTO.CreateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.ProjectResponse
import com.devlabs.devlabsbackend.project.domain.DTO.UpdateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.toProjectResponse
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
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
){    fun createProject(projectData: CreateProjectRequest): Project {
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
    fun updateProject(projectId: UUID, updateData: UpdateProjectRequest, requesterId: UUID): Project {
        val project = getProjectById(projectId)

        // Initialize team members to prevent LazyInitializationException
        project.team.members.size

        // Removed team membership restriction - anyone can now update projects
        if (project.status !in listOf(ProjectStatus.PROPOSED, ProjectStatus.REJECTED)) {
            throw IllegalArgumentException("Project cannot be edited in current status: ${project.status}")
        }
        updateData.title?.let { project.title = it }
        updateData.description?.let { project.description = it }
        updateData.objectives?.let { project.objectives = it }
        updateData.githubUrl?.let { project.githubUrl = it }

        project.updatedAt = Timestamp.from(Instant.now())        // Initialize course data if present
        project.courses.forEach { course ->
            course.students.size
            course.instructors.size
        }

        return projectRepository.save(project)
    }    fun updateProjectCourses(projectId: UUID, courseIds: List<UUID>, requesterId: UUID): Project {
        val project = getProjectById(projectId)
        
        // Initialize team members to prevent LazyInitializationException
        project.team.members.size
        
        // Check if user has permission to update the project
        val user = userRepository.findById(requesterId).orElseThrow {
            NotFoundException("User with id $requesterId not found")
        }
        
        if (project.status !in listOf(ProjectStatus.PROPOSED, ProjectStatus.REJECTED)) {
            throw IllegalArgumentException("Project courses cannot be updated in current status: ${project.status}")
        }
        
        // Initialize current courses collection
        project.courses.size
        
        // Update courses
        val newCourses = if (courseIds.isNotEmpty()) {
            courseRepository.findAllById(courseIds).also { foundCourses ->
                if (foundCourses.size != courseIds.size) {
                    throw NotFoundException("Some courses were not found")
                }
                foundCourses.forEach { it.students.size }
            }.toMutableSet()
        } else {
            mutableSetOf()
        }
        
        project.courses.clear()
        project.courses.addAll(newCourses)
        
        // Set status based on courses presence
        if (newCourses.isNotEmpty()) {
            project.status = ProjectStatus.ONGOING
        } else if (project.status == ProjectStatus.ONGOING) {
            // Only change status if it's currently ONGOING
            project.status = ProjectStatus.PROPOSED
        }
        
        project.updatedAt = Timestamp.from(Instant.now())
        
        return projectRepository.save(project)
    }
      fun approveProject(projectId: UUID, instructorId: UUID): Project {
        val project = getProjectById(projectId)
        val instructor = userRepository.findById(instructorId).orElseThrow {
            NotFoundException("User with id $instructorId not found")
        }
        
        // Initialize courses collection
        project.courses.size
        
        // Check if instructor can approve this project
        if (project.courses.isNotEmpty()) {
            val canApprove = project.courses.any { course ->
                course.instructors.size // Initialize instructors collection
                course.instructors.contains(instructor)
            }
            if (!canApprove) {
                throw IllegalArgumentException("Only course instructors can approve projects")
            }
        }
        
        if (project.status != ProjectStatus.PROPOSED) {
            throw IllegalArgumentException("Project cannot be approved in current status: ${project.status}")
        }

        project.status = ProjectStatus.ONGOING
        project.updatedAt = Timestamp.from(Instant.now())
        return projectRepository.save(project)
    }    fun rejectProject(projectId: UUID, instructorId: UUID, reason: String?): Project {
        val project = getProjectById(projectId)
        val instructor = userRepository.findById(instructorId).orElseThrow {
            NotFoundException("User with id $instructorId not found")
        }
        
        // Initialize courses collection
        project.courses.size
        
        // Check if instructor can reject this project
        if (project.courses.isNotEmpty()) {
            val canReject = project.courses.any { course ->
                course.instructors.size // Initialize instructors collection
                course.instructors.contains(instructor)
            }
            if (!canReject) {
                throw IllegalArgumentException("Only course instructors can reject projects")
            }
        }
        
        if (project.status != ProjectStatus.PROPOSED) {
            throw IllegalArgumentException("Project cannot be rejected in current status: ${project.status}")
        }
        project.status = ProjectStatus.REJECTED
        project.updatedAt = Timestamp.from(Instant.now())
        
        return projectRepository.save(project)
    }

    fun completeProject(projectId: UUID): Project {
        val project = getProjectById(projectId)
        if (project.status != ProjectStatus.ONGOING) {
            throw IllegalArgumentException("Project cannot be completed in current status: ${project.status}")
        }
        project.status = ProjectStatus.COMPLETED
        project.updatedAt = Timestamp.from(Instant.now())
        return projectRepository.save(project)
    }

    fun autoCompleteProjectsForInactiveCourses(): List<Project> {
        val projectsToComplete = projectRepository.findProjectsToAutoComplete()
        return projectsToComplete.map { project ->
            project.status = ProjectStatus.COMPLETED
            project.updatedAt = Timestamp.from(Instant.now())
            projectRepository.save(project)
        }
    }    fun getProjectsByTeam(teamId: UUID, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }
        
        // Initialize members collection to avoid LazyInitializationException
        team.members.size
        
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByTeam(team, pageable)
          // Initialize each project's related collections
        projectsPage.content.forEach { project ->
            project.courses.forEach { course -> 
                course.students.size
                course.instructors.size
            }
            // Ensure team is properly initialized for each project
            project.team.members.size
            // Initialize reviews collection to avoid LazyInitializationException
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
    }    fun getProjectsByCourse(courseId: UUID, page: Int = 0, size: Int = 10, sortBy: String = "title", sortOrder: String = "asc"): PaginatedResponse<ProjectResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Initialize course collections to avoid LazyInitializationException
        course.students.size
        course.instructors.size
        
        val sort = createSort(sortBy, sortOrder)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val projectsPage = projectRepository.findByCourse(course, pageable)
        
        // Initialize each project's team to avoid LazyInitializationException
        projectsPage.content.forEach { project ->
            project.team.members.size
            // Initialize courses collection to avoid LazyInitializationException
            project.courses.forEach { c ->
                c.students.size
                c.instructors.size
            }
            // Initialize reviews collection to avoid LazyInitializationException
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
    }fun getProjectsForUser(userId: UUID, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val teams = teamRepository.findByMemberList(user)

        teams.forEach { it.members.size }
        
        // Get all projects for these teams
        val projects = teams.flatMap { team ->
            projectRepository.findByTeam(team)
        }
            // Initialize collections for each project
        projects.forEach { project -> 
            project.courses.forEach { course ->
                course.students.size 
                course.instructors.size
            }
            // Initialize reviews collection
            project.reviews.size
        }
        
        // Apply pagination manually since we're doing a flatMap operation
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
    }    fun searchProjects(query: String, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByTitleContainingIgnoreCase(query, pageable)
        
        // Initialize lazy-loaded collections
        projectsPage.content.forEach { project ->
            project.team.members.size
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            // Initialize reviews collection
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
    
    fun searchProjectsByTeam(teamId: UUID, query: String, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }
        
        // Initialize team.members to avoid LazyInitializationException
        team.members.size
        
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByTeamAndTitleContainingIgnoreCase(team, query, pageable)
          // Initialize lazy-loaded collections for each project
        projectsPage.content.forEach { project ->
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            // Initialize reviews collection
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
      fun searchProjectsByCourse(courseId: UUID, query: String, page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Initialize course collections to avoid LazyInitializationException
        course.students.size
        course.instructors.size
        
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByCourseAndTitleContainingIgnoreCase(course, query, pageable)
        
        // Initialize each project's team to avoid LazyInitializationException
        projectsPage.content.forEach { project ->
            project.team.members.size
            // Initialize courses collection to avoid LazyInitializationException
            project.courses.forEach { c ->
                c.students.size
                c.instructors.size
            }
            // Initialize reviews collection to avoid LazyInitializationException
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
    }    fun getProjectsNeedingApproval(page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByStatus(ProjectStatus.PROPOSED, pageable)
        
        // Initialize lazy-loaded collections
        projectsPage.content.forEach { project ->
            project.team.members.size
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            // Initialize reviews collection
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
    
    fun getOngoingProjects(page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val projectsPage = projectRepository.findByStatus(ProjectStatus.ONGOING, pageable)
          // Initialize lazy-loaded collections
        projectsPage.content.forEach { project ->
            project.team.members.size
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            // Initialize reviews collection
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
        )    }fun getProjectById(projectId: UUID): Project {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
          // Initialize lazy-loaded collections to avoid LazyInitializationException
        project.team.members.size
        project.courses.forEach { course ->
            course.students.size
            course.instructors.size
        }
        // Initialize reviews collection
        project.reviews.size
        
        return project
    }
    
    fun getAllProjectsByTeam(teamId: UUID): List<ProjectResponse> {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }
        
        // Initialize members collection to avoid LazyInitializationException
        team.members.size
        
        val projects = projectRepository.findByTeam(team)
          // Initialize each project's related collections
        projects.forEach { project ->
            project.courses.forEach { course ->
                course.students.size
                course.instructors.size
            }
            // Ensure team is properly initialized for each project
            project.team.members.size
            // Initialize reviews collection to avoid LazyInitializationException
            project.reviews.size
        }
        
        return projects.map { it.toProjectResponse() }
    }
    
    fun deleteProject(projectId: UUID, userId: UUID): Boolean {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Verify user has permission to delete the project
        // You might want to add additional permission checks here based on your business logic
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
          // Check if user is part of the team or an instructor of any course
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

    private fun createSort(sortBy: String, sortOrder: String): Sort {
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(direction, sortBy)
    }    @Transactional
    fun getAllActiveProjects(): List<ProjectResponse> {
        return projectRepository.findByStatusIn(listOf(ProjectStatus.ONGOING, ProjectStatus.PROPOSED))
            .map { it.toProjectResponse() }
    }
    
    @Transactional
    fun getActiveProjectsBySemester(semesterId: UUID): List<ProjectResponse> {
        return projectRepository.findActiveProjectsBySemester(semesterId)
            .map { it.toProjectResponse() }
    }
    
    @Transactional
    fun getActiveProjectsByBatch(batchId: UUID): List<ProjectResponse> {
        return projectRepository.findActiveProjectsByBatch(batchId)
            .map { it.toProjectResponse() }
    }
}
