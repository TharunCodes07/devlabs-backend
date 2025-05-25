package com.devlabs.devlabsbackend.semester.domain

import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.util.*



@Entity
@Table(name = "semester")
class Semester(
    @Id
    @GeneratedValue
    var id: UUID? = null,
    val name: String,
    val year: Int,
    val isActive: Boolean = true,

//    @OneToMany(fetch = FetchType.LAZY,cascade = [CascadeType.ALL], mappedBy = "semester")
//    val courses: MutableList<Course> = mutableListOf(),

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "semester_managers",
        joinColumns = [JoinColumn(name = "semester_id")],
        inverseJoinColumns = [JoinColumn(name = "manager_id")]
    )
    val managers: MutableList<User> = mutableListOf(),

    )