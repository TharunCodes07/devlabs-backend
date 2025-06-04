package com.devlabs.devlabsbackend.individualscore.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "individual_score")
class IndividualScore(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var score: Double = 0.0,

    val participantId: UUID,

    @ManyToOne
    @JoinColumn(name = "criterion_id")
    val criterion: Criterion
)