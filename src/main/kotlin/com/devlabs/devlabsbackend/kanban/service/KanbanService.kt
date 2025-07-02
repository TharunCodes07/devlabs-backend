package com.devlabs.devlabsbackend.kanban.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.kanban.domain.DTO.*
import com.devlabs.devlabsbackend.kanban.domain.KanbanBoard
import com.devlabs.devlabsbackend.kanban.domain.KanbanTask
import com.devlabs.devlabsbackend.kanban.repository.KanbanBoardRepository
import com.devlabs.devlabsbackend.kanban.repository.KanbanColumnRepository
import com.devlabs.devlabsbackend.kanban.repository.KanbanTaskRepository
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

//     @Cacheable(value = [CacheConfig.KANBAN_CACHE], key = "'board_response_project_' + #projectId")
    fun getOrCreateBoardForProject(projectId: UUID): KanbanBoardResponse {
        val project = projectRepository.findByIdWithTeamAndMembers(projectId) ?: throw NotFoundException("Project with id $projectId not found")
        
        val board = kanbanBoardRepository.findByProjectWithRelations(project) ?: run {
            val newBoard = KanbanBoard(project = project)
            newBoard.initializeDefaultColumns()
            kanbanBoardRepository.save(newBoard)
            kanbanBoardRepository.findByProjectWithRelations(project)!!
        }

        return board.toBoardResponse()
    }

    // @CacheEvict(
    //     value = [CacheConfig.KANBAN_CACHE], 
    //     allEntries = true
    // )
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
        )
        
        val savedTask = kanbanTaskRepository.save(task)
        return savedTask.toTaskResponse()
    }

    // @CacheEvict(
    //     value = [CacheConfig.KANBAN_CACHE], 
    //     allEntries = true
    // )
    fun updateTask(taskId: UUID, request: UpdateTaskRequest, userId: String): KanbanTaskResponse {
        val task = kanbanTaskRepository.findByIdWithRelations(taskId) ?: throw NotFoundException("Task with id $taskId not found")
        
        val requester = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        if (!task.column.board.project.team.members.contains(requester)) {
            throw IllegalArgumentException("Only team members can update tasks")
        }
        
        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        
        task.updatedAt = Timestamp.from(Instant.now())
        
        val savedTask = kanbanTaskRepository.save(task)
        return savedTask.toTaskResponse()
    }

    // @CacheEvict(
    //     value = [CacheConfig.KANBAN_CACHE], 
    //     allEntries = true
    // )
    fun moveTask(taskId: UUID, request: MoveTaskRequest, userId: String): KanbanTaskResponse {
        val task = kanbanTaskRepository.findByIdWithRelations(taskId) ?: throw NotFoundException("Task with id $taskId not found")
        
        val newColumn = kanbanColumnRepository.findById(request.columnId).orElseThrow {
            NotFoundException("Column with id ${request.columnId} not found")
        }
        
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

    // @CacheEvict(
    //     value = [CacheConfig.KANBAN_CACHE], 
    //     allEntries = true
    // )
    fun deleteTask(taskId: UUID) {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }
        
        kanbanTaskRepository.delete(task)
    }


    // @Cacheable(value = [CacheConfig.KANBAN_CACHE], key = "'task_' + #taskId")
    fun getTaskById(taskId: UUID): KanbanTaskResponse {
        val task = kanbanTaskRepository.findById(taskId).orElseThrow {
            NotFoundException("Task with id $taskId not found")
        }

        task.createdBy.name
        
        return task.toTaskResponse()
    }
}
