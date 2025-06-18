package com.devlabs.devlabsbackend.individualscore.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "individual_score")
class IndividualScore(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    val participant: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id")
    val criterion: Criterion,

    @Column(name = "score")
    var score: Double,

    @Column(columnDefinition = "TEXT")
    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = true)
    val course: com.devlabs.devlabsbackend.course.domain.Course? = null
)