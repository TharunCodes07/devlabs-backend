package com.devlabs.devlabsbackend.evaluation.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "evaluation")
class Evaluation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: Project,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id")
    val evaluator: User,
    
    var comments: String? = null,
    
    @OneToMany(mappedBy = "evaluation", cascade = [CascadeType.ALL], orphanRemoval = true)
    val criterionScores: MutableSet<CriterionScore> = mutableSetOf(),
    
    var status: EvaluationStatus = EvaluationStatus.DRAFT,
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
)

enum class EvaluationStatus {
    DRAFT,
    SUBMITTED,
    COMPLETED
}
