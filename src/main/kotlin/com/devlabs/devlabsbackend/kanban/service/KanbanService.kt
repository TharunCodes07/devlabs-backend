package com.devlabs.devlabsbackend.kanban.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.kanban.domain.*
import com.devlabs.devlabsbackend.kanban.domain.DTO.*
import com.devlabs.devlabsbackend.kanban.repository.*
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class KanbanService(
    private val kanbanBoardRepository: KanbanBoardRepository,
    private val kanbanColumnRepository: KanbanColumnRepository,
    private val kanbanTaskRepository: KanbanTaskRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {

    fun getOrCreateBoardForProject(projectId: UUID): KanbanBoardResponse {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        project.team.members.size
        
        val board = kanbanBoardRepository.findByProject(project) ?: run {
            val newBoard = KanbanBoard(project = project)
            newBoard.initializeDefaultColumns()
            kanbanBoardRepository.save(newBoard)
        }

        board.columns.forEach { column ->
            column.tasks.size
            column.tasks.forEach { task ->
                task.createdBy.name
                task.assignedTo?.name
            }
        }
        
        return board.toBoardResponse()
    }

    fun createTask(request: CreateTaskRequest, userId: String): KanbanTaskResponse {
        val column = kanbanColumnRepository.findById(request.columnId).orElseThrow {
            NotFoundException("Column with id ${request.columnId} not found")
        }
        
        val createdBy = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        val assignedTo = request.assignedToId?.let { assignedToId ->
            userRepository.findById(assignedToId).orElseThrow {
                NotFoundException("User with id $assignedToId not found")
            }
        }

        val maxPosition = kanbanTaskRepository.findMaxPositionInColumn(column) ?: -1
        
        val task = KanbanTask(
            title = request.title,
            description = request.description,
            position = maxPosition + 1,
            column = column,
            createdBy = createdBy,
            assignedTo = assignedTo
        )
        
        val savedTask = kanbanTaskRepository.save(task)
        return savedTask.toTaskResponse()
    }

    fun updateTask(taskId: UUID, request: UpdateTaskRequest, userId: String): KanbanTaskResponse {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }

        task.createdBy.name
        task.assignedTo?.name
        task.column.board.project.team.members.size
        
        val requester = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        if (!task.column.board.project.team.members.contains(requester)) {
            throw IllegalArgumentException("Only team members can update tasks")
        }
        
        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        request.assignedToId?.let { assignedToId ->
            val assignedTo = userRepository.findById(assignedToId).orElseThrow {
                NotFoundException("User with id $assignedToId not found")
            }
            task.assignedTo = assignedTo
        }
        
        task.updatedAt = Timestamp.from(Instant.now())
        
        val savedTask = kanbanTaskRepository.save(task)
        return savedTask.toTaskResponse()
    }

    fun moveTask(taskId: UUID, request: MoveTaskRequest, userId: String): KanbanTaskResponse {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }
        
        val newColumn = kanbanColumnRepository.findById(request.columnId).orElseThrow {
            NotFoundException("Column with id ${request.columnId} not found")
        }

        task.createdBy.name
        task.assignedTo?.name
        task.column.board.project.team.members.size
        
        val requester = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        if (!task.column.board.project.team.members.contains(requester)) {
            throw IllegalArgumentException("Only team members can move tasks")
        }

        task.column = newColumn
        task.position = request.position
        task.updatedAt = Timestamp.from(Instant.now())
        
        val savedTask = kanbanTaskRepository.save(task)
        return savedTask.toTaskResponse()
    }

    fun deleteTask(taskId: UUID) {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }
        
        kanbanTaskRepository.delete(task)
    }


    fun getTaskById(taskId: UUID): KanbanTaskResponse {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }

        task.createdBy.name
        task.assignedTo?.name
        
        return task.toTaskResponse()
    }
}
