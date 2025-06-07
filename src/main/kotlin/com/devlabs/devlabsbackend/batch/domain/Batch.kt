package com.devlabs.devlabsbackend.batch.domain

import com.devlabs.devlabsbackend.department.domain.Department
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.time.Year
import java.util.*

@Entity
@Table(name = "batch")
class Batch (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id : UUID? = null,
    var name: String,
    var graduationYear: Year,
    var section: String,
    var isActive: Boolean,

    @OneToMany(fetch = FetchType.LAZY)
    val students: MutableSet<User> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    val managers: MutableSet<User> = mutableSetOf(),
    @OneToMany(fetch = FetchType.LAZY, cascade =  [CascadeType.ALL])
    val semester: MutableSet<Semester> = mutableSetOf(),

    @ManyToOne
    @JoinColumn(name = "department_id")
    var department: Department? = null,

    )