package com.devlabs.devlabsbackend.course.domain

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.semester.domain.Semester
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.util.*

enum class CourseType{
    CORE,
    ELECTIVE,
    MICRO_CREDENTIAL
}
@Entity
@Table(name = "course")
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    var description: String,
    var type: CourseType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    var semester: Semester,

    @OneToMany(fetch = FetchType.LAZY)
    var students: MutableSet<User> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY)
    var instructors: MutableSet<User> = mutableSetOf(),

    @OneToMany(fetch = FetchType.LAZY)
    var batches: MutableSet<Batch> = mutableSetOf()
)
