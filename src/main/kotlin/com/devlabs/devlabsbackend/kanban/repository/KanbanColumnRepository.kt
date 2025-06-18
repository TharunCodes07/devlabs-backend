package com.devlabs.devlabsbackend.kanban.repository

import com.devlabs.devlabsbackend.kanban.domain.KanbanColumn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "kanban-columns")
interface KanbanColumnRepository : JpaRepository<KanbanColumn, UUID>
