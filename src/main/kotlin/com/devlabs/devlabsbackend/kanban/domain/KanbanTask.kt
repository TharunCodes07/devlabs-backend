package com.devlabs.devlabsbackend.kanban.domain

import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "kanban_task")
class KanbanTask(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var title: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    var position: Int,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id")
    var column: KanbanColumn,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = true)
    var assignedTo: User? = null,
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
)
