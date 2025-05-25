package com.devlabs.devlabsbackend.batch.domain

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
    val name: String,
    val batch: Year,
    val department: String,
    val section: String,
    val isActive: Boolean,

    @OneToMany(fetch = FetchType.LAZY)
    val students: MutableSet<User> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    val managers: MutableSet<User> = mutableSetOf(),

//    @OneToMany(fetch = FetchType.LAZY , cascade = [CascadeType.ALL])
//    val semesters = MutableSet<Semester> = mutableSetOf()
    )