package com.devlabs.devlabsbackend.team.repository

import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "teams")
interface TeamRepository : JpaRepository<Team, UUID> {

    @Query("SELECT t FROM Team t JOIN t.members m WHERE m = :member")
    fun findByMember(@Param("member") member: User): List<Team>

    fun findByNameContainingIgnoreCase(name: String): List<Team>
}
