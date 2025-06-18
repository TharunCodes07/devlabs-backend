package com.devlabs.devlabsbackend.criterion.domain

import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "criterion")
class Criterion(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    var description: String,
    var maxScore: Float,
    var isCommon: Boolean,

    @ManyToOne
    @JoinColumn(name = "rubrics_id")
    var rubrics: Rubrics? = null
)