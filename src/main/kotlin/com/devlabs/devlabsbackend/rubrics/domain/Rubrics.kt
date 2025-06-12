package com.devlabs.devlabsbackend.rubrics.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "rubrics")
class Rubrics (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    var createdBy: User,
    
    var createdAt: Timestamp = Timestamp.from(Instant.now()),

    @OneToMany(mappedBy = "rubrics")
    var criteria: MutableSet<Criterion> = mutableSetOf(),
    
    var isShared: Boolean = false,
    
    // This field is kept only for database compatibility, will be removed in future migrations
    @Column(name = "is_active", nullable = true)
    var isActive: Boolean? = true
)