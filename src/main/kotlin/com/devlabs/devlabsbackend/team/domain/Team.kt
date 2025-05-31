package com.devlabs.devlabsbackend.team.domain

import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.user.domain.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "team")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var name: String,
    var description: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_members",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var members: MutableSet<User> = mutableSetOf(),    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JsonIgnore
    var projects: MutableSet<Project> = mutableSetOf(),
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
) {
    fun isMember(user: User): Boolean = members.any { it.id == user.id }
}
