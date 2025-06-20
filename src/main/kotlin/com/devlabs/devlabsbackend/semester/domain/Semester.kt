package com.devlabs.devlabsbackend.semester.domain

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.course.domain.Course
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "semester")
class Semester(    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    var name: String,
    var year: Int,
    var isActive: Boolean = true,

    @OneToMany(fetch = FetchType.LAZY,cascade = [CascadeType.ALL], mappedBy = "semester")
    var courses: MutableList<Course> = mutableListOf(),

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "semester_managers",
        joinColumns = [JoinColumn(name = "semester_id")],
        inverseJoinColumns = [JoinColumn(name = "manager_id")]
    )
    var managers: MutableList<User> = mutableListOf(),

    @ManyToMany(mappedBy = "semester", fetch = FetchType.LAZY)
    var batches: MutableSet<Batch> = mutableSetOf(),
    )