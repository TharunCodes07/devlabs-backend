package com.devlabs.devlabsbackend.project.domain

import com.devlabs.devlabsbackend.course.domain.Course
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.team.domain.Team
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

enum class ProjectStatus {
    PROPOSED,
    ONGOING,
    COMPLETED,
    REJECTED
}

@Entity
@Table(name = "project")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var title: String,
      @Column(columnDefinition = "TEXT")
    var description: String,
    
    @Column(columnDefinition = "TEXT")
    var objectives: String? = null,
    
    var status: ProjectStatus = ProjectStatus.PROPOSED,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course,

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var reviews: MutableSet<Review> = mutableSetOf(),
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
)
