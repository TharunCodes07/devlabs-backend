package com.devlabs.devlabsbackend.kanban.controller

import com.devlabs.devlabsbackend.kanban.domain.DTO.CreateTaskRequest
import com.devlabs.devlabsbackend.kanban.domain.DTO.MoveTaskRequest
import com.devlabs.devlabsbackend.kanban.domain.DTO.UpdateTaskRequest
import com.devlabs.devlabsbackend.kanban.service.KanbanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/kanban")
class KanbanController(
    private val kanbanService: KanbanService
) {
    @GetMapping("/project/{projectId}")
    fun getKanbanBoard(@PathVariable projectId: UUID): ResponseEntity<Any> {
        return try {
            val board = kanbanService.getOrCreateBoardForProject(projectId)
            ResponseEntity.ok(board)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get kanban board: ${e.message}"))
        }
    }

    @PostMapping("/tasks")
    fun createTask(
        @RequestBody request: CreateTaskRequest
    ): ResponseEntity<Any> {
        return try {
            val task = kanbanService.createTask(request, request.userId)
            ResponseEntity.status(HttpStatus.CREATED).body(task)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create task: ${e.message}"))
        }
    }

    @PutMapping("/tasks/{taskId}")
    fun updateTask(
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<Any> {
        return try {
            val task = kanbanService.updateTask(taskId, request, request.userId)
            ResponseEntity.ok(task)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update task: ${e.message}"))
        }
    }

    @PutMapping("/tasks/{taskId}/move")
    fun moveTask(
        @PathVariable taskId: UUID,
        @RequestBody request: MoveTaskRequest
    ): ResponseEntity<Any> {
        return try {
            val task = kanbanService.moveTask(taskId, request, request.userId)
            ResponseEntity.ok(task)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to move task: ${e.message}"))
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    fun deleteTask(
        @PathVariable taskId: UUID
    ): ResponseEntity<Any> {
        return try {
            kanbanService.deleteTask(taskId)
            ResponseEntity.ok(mapOf("message" to "Task deleted successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete task: ${e.message}"))
        }
    }

    @GetMapping("/tasks/{taskId}")
    fun getTask(@PathVariable taskId: UUID): ResponseEntity<Any> {
        return try {
            val task = kanbanService.getTaskById(taskId)
            ResponseEntity.ok(task)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get task: ${e.message}"))
        }
    }
}
