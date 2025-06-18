package com.devlabs.devlabsbackend.kanban.domain

import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "kanban_column")
class KanbanColumn(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var name: String,
    var position: Int,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    var board: KanbanBoard,
    
    @OneToMany(mappedBy = "column", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var tasks: MutableSet<KanbanTask> = mutableSetOf(),
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
)
