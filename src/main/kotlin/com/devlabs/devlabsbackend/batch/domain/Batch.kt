package com.devlabs.devlabsbackend.batch.domain

import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.time.Year
import java.util.*

@Entity
@Table(name = "batch")
class Batch (    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id : UUID? = null,
    var name: String,
    var graduationYear: Year,
    var section: String,
    var isActive: Boolean,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "batch_student",
        joinColumns = [JoinColumn(name = "batch_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")]
    )
    val students: MutableSet<User> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "batch_manager",
        joinColumns = [JoinColumn(name = "batch_id")],
        inverseJoinColumns = [JoinColumn(name = "manager_id")]
    )
    val managers: MutableSet<User> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "batch_semester",
        joinColumns = [JoinColumn(name = "batch_id")],
        inverseJoinColumns = [JoinColumn(name = "semester_id")]
    )
    val semester: MutableSet<Semester> = mutableSetOf(),

    @ManyToOne
    @JoinColumn(name = "department_id")
    var department: Department? = null,

    )