package com.devlabs.devlabsbackend.evaluation.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "criterion_score")
class CriterionScore(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    var evaluation: Evaluation? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id")
    val criterion: Criterion,

    var score: Float,

    var comment: String? = null
)
