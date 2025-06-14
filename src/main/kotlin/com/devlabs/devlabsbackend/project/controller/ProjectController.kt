package com.devlabs.devlabsbackend.project.controller

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.project.domain.DTO.*
import com.devlabs.devlabsbackend.project.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService,
    private val reviewService: com.devlabs.devlabsbackend.review.service.ReviewService
) {

    /**
     * Create a new project
     */
    @PostMapping
    fun createProject(@RequestBody request: CreateProjectRequest): ResponseEntity<Any> {
        return try {
            val project = projectService.createProject(request)
            ResponseEntity.status(HttpStatus.CREATED).body(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create project: ${e.message}"))
        }
    }

    /**
     * Update an existing project
     */
    @PutMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateProjectRequest
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.updateProject(projectId, request, request.userId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update project: ${e.message}"))
        }
    }

    /**
     * Update project courses
     */
    @PutMapping("/{projectId}/courses")
    fun updateProjectCourses(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateProjectCoursesRequest
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.updateProjectCourses(projectId, request.courseIds, request.userId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update project courses: ${e.message}"))
        }
    }

    /**
     * Approve a project
     */
    @PutMapping("/{projectId}/approve")
    fun approveProject(
        @PathVariable projectId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.approveProject(projectId, request.userId)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to approve project: ${e.message}"))
        }
    }

    /**
     * Reject a project with optional reason
     */
    @PutMapping("/{projectId}/reject")
    fun rejectProject(
        @PathVariable projectId: UUID,
        @RequestBody request: RejectProjectRequest
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.rejectProject(projectId, request.userId, request.reason)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to reject project: ${e.message}"))
        }
    }

    /**
     * Mark a project as complete
     */
    @PutMapping("/{projectId}/complete")
    fun completeProject(
        @PathVariable projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.completeProject(projectId)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to complete project: ${e.message}"))
        }
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{projectId}")
    fun getProjectById(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val project = projectService.getProjectById(projectId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get project: ${e.message}"))
        }
    }

    /**
     * Auto-complete projects for inactive courses
     */
    @PostMapping("/auto-complete")
    fun autoCompleteProjects(): ResponseEntity<Any> {
        return try {
            val projects = projectService.autoCompleteProjectsForInactiveCourses()
            ResponseEntity.ok(mapOf("completed" to projects.size, "projects" to projects))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to auto-complete projects: ${e.message}"))
        }
    }

    /**
     * Get projects by team with pagination
     */
    @GetMapping("/team/{teamId}")
    fun getProjectsByTeam(
        @PathVariable teamId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsByTeam(teamId, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects by team: ${e.message}"))
        }
    }

    /**
     * Get all projects by team (no pagination)
     */
    @GetMapping("/team/{teamId}/all")
    fun getAllProjectsByTeam(@PathVariable teamId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getAllProjectsByTeam(teamId)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get all projects for team: ${e.message}"))
        }
    }

    /**
     * Get projects by course with pagination and sorting
     */
    @GetMapping("/course/{courseId}")
    fun getProjectsByCourse(
        @PathVariable courseId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "title") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") sortOrder: String
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsByCourse(courseId, page, size, sortBy, sortOrder)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects by course: ${e.message}"))
        }
    }

    /**
     * Search projects by course
     */
    @GetMapping("/course/{courseId}/search")
    fun searchProjectsByCourse(
        @PathVariable courseId: UUID,
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchProjectsByCourse(courseId, query, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects for course: ${e.message}"))
        }
    }

    /**
     * Get projects for a specific user
     */
    @GetMapping("/user/{userId}")
    fun getProjectsForUser(
        @PathVariable userId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsForUser(userId, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects for user: ${e.message}"))
        }
    }

    /**
     * Search projects globally
     */
    @GetMapping("/search")
    fun searchProjects(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchProjects(query, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects: ${e.message}"))
        }
    }

    /**
     * Search projects by team
     */
    @GetMapping("/team/{teamId}/search")
    fun searchProjectsByTeam(
        @PathVariable teamId: UUID,
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchProjectsByTeam(teamId, query, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects for team: ${e.message}"))
        }
    }

    /**
     * Get projects needing approval
     */
    @GetMapping("/pending-approval")
    fun getProjectsNeedingApproval(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsNeedingApproval(page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects needing approval: ${e.message}"))
        }
    }

    /**
     * Get ongoing/active projects
     */
    @GetMapping("/ongoing")
    fun getOngoingProjects(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getOngoingProjects(page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get ongoing projects: ${e.message}"))
        }
    }

    /**
     * Get active projects (alias for ongoing projects)
     */
    @GetMapping("/active")
    fun getActiveProjects(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getOngoingProjects(page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get active projects: ${e.message}"))
        }
    }

    /**
     * Delete a project
     */
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: UUID,
        @RequestBody request: UserIdRequest
    ): ResponseEntity<Any> {
        return try {
            projectService.deleteProject(projectId, request.userId)
            ResponseEntity.ok(mapOf("success" to true, "message" to "Project deleted successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete project: ${e.message}"))
        }
    }

    /**
     * Get archived (completed) projects for a user based on their role
     */
    @GetMapping("/user/{userId}/archive")
    fun getArchivedProjects(
        @PathVariable userId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getArchivedProjects(userId, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get archived projects: ${e.message}"))
        }
    }

    /**
     * Search archived (completed) projects for a user based on their role
     */
    @GetMapping("/user/{userId}/archive/search")
    fun searchArchivedProjects(
        @PathVariable userId: UUID,
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchArchivedProjects(userId, query, page, size)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search archived projects: ${e.message}"))
        }
    }

    /**
     * Get reviews assigned to a project
     */
    @GetMapping("/{projectId}/reviews")
    fun getProjectReviews(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val response = reviewService.checkProjectReviewAssignment(projectId)
            ResponseEntity.ok(response)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to check project reviews: ${e.message}"))
        }
    }
}
