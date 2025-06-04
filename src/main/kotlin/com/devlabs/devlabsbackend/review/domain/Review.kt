package com.devlabs.devlabsbackend.review.domain

import com.devlabs.devlabsbackend.course.domain.Course
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "review")
class Review (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    val courses: MutableSet<Course> = mutableSetOf(),

    val section: MutableSet<String>,

    @OneToOne
    @JoinColumn(name = "rubrics_id")
    val rubrics: Rubrics? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: Project? = null,
    )

