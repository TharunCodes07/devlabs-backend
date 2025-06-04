package com.devlabs.devlabsbackend.kanban.domain

import com.devlabs.devlabsbackend.project.domain.Project
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "kanban_board")
class KanbanBoard(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    var project: Project,
    
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var columns: MutableSet<KanbanColumn> = mutableSetOf(),
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
) {
    fun initializeDefaultColumns() {
        if (columns.isEmpty()) {
            columns.add(KanbanColumn(name = "To-Do", position = 0, board = this))
            columns.add(KanbanColumn(name = "In Progress", position = 1, board = this))
            columns.add(KanbanColumn(name = "Completed", position = 2, board = this))
        }
    }
}
