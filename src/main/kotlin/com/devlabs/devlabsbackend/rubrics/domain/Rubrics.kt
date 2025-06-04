package com.devlabs.devlabsbackend.rubrics.domain

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID


@Entity
@Table(name = "rubrics")
class Rubrics (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,

    @OneToMany(mappedBy = "rubrics")
    var criteria: MutableSet<Criterion> = mutableSetOf(),

    )