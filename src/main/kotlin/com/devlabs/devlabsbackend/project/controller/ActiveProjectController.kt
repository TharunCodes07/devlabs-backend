package com.devlabs.devlabsbackend.project.controller

import com.devlabs.devlabsbackend.project.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/project")
class ActiveProjectController(
    private val projectService: ProjectService
) {
    
    @GetMapping("/active")
    fun getAllActiveProjects(): ResponseEntity<Any> {
        return try {
            val projects = projectService.getAllActiveProjects()
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to retrieve active projects: ${e.message}"))
        }
    }
    
    @GetMapping("/semester/{semesterId}/active")
    fun getActiveProjectsBySemester(@PathVariable semesterId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getActiveProjectsBySemester(semesterId)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to retrieve active projects for semester: ${e.message}"))
        }
    }
    
    @GetMapping("/batch/{batchId}/active")
    fun getActiveProjectsByBatch(@PathVariable batchId: UUID): ResponseEntity<Any> {
        return try {
            val projects = projectService.getActiveProjectsByBatch(batchId)
            ResponseEntity.ok(projects)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to retrieve active projects for batch: ${e.message}"))
        }
    }
}