package com.devlabs.devlabsbackend.department.domain

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.semester.domain.Semester
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "department")
class Department (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    @OneToMany(mappedBy = "department", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val batches: MutableSet<Batch> = mutableSetOf(),
)