package com.devlabs.devlabsbackend.kanban.repository

import com.devlabs.devlabsbackend.kanban.domain.KanbanBoard
import com.devlabs.devlabsbackend.project.domain.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "kanban-boards")
interface KanbanBoardRepository : JpaRepository<KanbanBoard, UUID> {
    
    fun findByProject(project: Project): KanbanBoard?
    
    @Query("SELECT kb FROM KanbanBoard kb WHERE kb.project.id = :projectId")
    fun findByProjectId(@Param("projectId") projectId: UUID): KanbanBoard?
}
