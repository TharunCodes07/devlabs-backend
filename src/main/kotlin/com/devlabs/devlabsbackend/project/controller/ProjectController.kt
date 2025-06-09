package com.devlabs.devlabsbackend.project.controller

import com.devlabs.devlabsbackend.project.domain.DTO.CreateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.UpdateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.toProjectResponse
import com.devlabs.devlabsbackend.project.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService
) {    @PostMapping
    fun createProject(@RequestBody request: CreateProjectRequest): ResponseEntity<Any> {
        return try {
            val project = projectService.createProject(request)
            ResponseEntity.status(HttpStatus.CREATED).body(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create project"))
        }
    }

    @PutMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateProjectRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {        return try {
            val project = projectService.updateProject(projectId, request, userId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update project"))        
        }
    }    @PutMapping("/{projectId}/courses")
    fun updateProjectCourses(
        @PathVariable projectId: UUID,
        @RequestBody courseIds: List<UUID>,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.updateProjectCourses(projectId, courseIds, userId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update project courses"))
        }
    }

    @PutMapping("/{projectId}/approve")
    fun approveProject(
        @PathVariable projectId: UUID,
        @RequestHeader("X-User-Id") instructorId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.approveProject(projectId, instructorId)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to approve project"))
        }
    }

    @PutMapping("/{projectId}/reject")
    fun rejectProject(
        @PathVariable projectId: UUID,
        @RequestBody request: RejectProjectRequest,
        @RequestHeader("X-User-Id") instructorId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.rejectProject(projectId, instructorId, request.reason)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))        }
        catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to reject project"))
        }
    }

    @PutMapping("/{projectId}/complete")
    fun completeProject(
        @PathVariable projectId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.completeProject(projectId)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to complete project"))
        }
    }

    @GetMapping("/{projectId}")
    fun getProjectById(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val project = projectService.getProjectById(projectId)
            ResponseEntity.ok(project.toProjectResponse())
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get project"))
        }
    }

    @PostMapping("/auto-complete")
    fun autoCompleteProjects(): ResponseEntity<Any> {
        return try {
            val projects = projectService.autoCompleteProjectsForInactiveCourses()
            ResponseEntity.ok(mapOf("completed" to projects.size, "projects" to projects))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to auto-complete projects"))
        }
    }    @GetMapping("/team/{teamId}")
    fun getProjectsByTeam(
        @PathVariable teamId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsByTeam(teamId, page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }

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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }

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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects for course"))
        }
    }

    @GetMapping("/user/{userId}")
    fun getProjectsForUser(
        @PathVariable userId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsForUser(userId, page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }    @GetMapping("/search")
    fun searchProjects(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchProjects(query, page, size)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects"))
        }
    }
    
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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects for team"))
        }
    }

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
                .body(mapOf("error" to "Failed to get projects needing approval"))
        }
    }

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
                .body(mapOf("error" to "Failed to get ongoing projects"))
        }
    }
    
    @GetMapping("/team/{teamId}/all")
    fun getAllProjectsByTeam(@PathVariable teamId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getAllProjectsByTeam(teamId)
            ResponseEntity.ok(projects)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get all projects for team"))
        }
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: UUID,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val result = projectService.deleteProject(projectId, userId)
            ResponseEntity.ok(mapOf("success" to true, "message" to "Project deleted successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete project"))
        }
    }
}

data class RejectProjectRequest(
    val reason: String? = null
)
