package com.devlabs.devlabsbackend.project.controller

import com.devlabs.devlabsbackend.project.domain.DTO.CreateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.UpdateProjectRequest
import com.devlabs.devlabsbackend.project.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping
    fun createProject(
        @RequestBody request: CreateProjectRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.createProject(request, userId)
            ResponseEntity.status(HttpStatus.CREATED).body(project)
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
    ): ResponseEntity<Any> {
        return try {
            val project = projectService.updateProject(projectId, request, userId)
            ResponseEntity.ok(project)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update project"))        }
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
            ResponseEntity.badRequest().body(mapOf("error" to e.message))        } catch (e: Exception) {
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

    @PostMapping("/auto-complete")
    fun autoCompleteProjects(): ResponseEntity<Any> {
        return try {
            val projects = projectService.autoCompleteProjectsForInactiveCourses()
            ResponseEntity.ok(mapOf("completed" to projects.size, "projects" to projects))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to auto-complete projects"))
        }
    }

    @GetMapping("/team/{teamId}")
    fun getProjectsByTeam(@PathVariable teamId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsByTeam(teamId)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }

    @GetMapping("/course/{courseId}")
    fun getProjectsByCourse(@PathVariable courseId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsByCourse(courseId)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }

    @GetMapping("/user/{userId}")
    fun getProjectsForUser(@PathVariable userId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsForUser(userId)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects"))
        }
    }

    @GetMapping("/search")
    fun searchProjects(@RequestParam query: String): ResponseEntity<Any> {
        return try {
            val projects = projectService.searchProjects(query)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search projects"))
        }
    }

    @GetMapping("/pending-approval")
    fun getProjectsNeedingApproval(): ResponseEntity<Any> {
        return try {
            val projects = projectService.getProjectsNeedingApproval()
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get projects needing approval"))
        }
    }

    @GetMapping("/ongoing")
    fun getOngoingProjects(): ResponseEntity<Any> {
        return try {
            val projects = projectService.getOngoingProjects()
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get ongoing projects"))
        }
    }
}

data class RejectProjectRequest(
    val reason: String? = null
)
