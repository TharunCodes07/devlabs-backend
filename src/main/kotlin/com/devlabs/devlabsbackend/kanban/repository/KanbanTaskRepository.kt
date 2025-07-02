package com.devlabs.devlabsbackend.kanban.repository

import com.devlabs.devlabsbackend.kanban.domain.KanbanColumn
import com.devlabs.devlabsbackend.kanban.domain.KanbanTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "kanban-tasks")
interface KanbanTaskRepository : JpaRepository<KanbanTask, UUID> {
    
    fun findByColumnOrderByPosition(column: KanbanColumn): List<KanbanTask>
    
    @Query("SELECT MAX(t.position) FROM KanbanTask t WHERE t.column = :column")
    fun findMaxPositionInColumn(@Param("column") column: KanbanColumn): Int?
    
    @Query("""
        SELECT DISTINCT t FROM KanbanTask t 
        LEFT JOIN FETCH t.createdBy cb
        LEFT JOIN FETCH t.column c
        LEFT JOIN FETCH c.board b
        LEFT JOIN FETCH b.project p
        LEFT JOIN FETCH p.team team
        LEFT JOIN FETCH team.members
        WHERE t.id = :taskId
    """)
    fun findByIdWithRelations(@Param("taskId") taskId: UUID): KanbanTask?
}
