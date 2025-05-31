package com.devlabs.devlabsbackend.team.repository

import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "teams")
interface TeamRepository : JpaRepository<Team, UUID> {    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)")
    fun findByMember(@Param("member") member: User): List<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.members m WHERE m = :member")
    fun findByMember(@Param("member") member: User, pageable: Pageable): Page<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<Team>
    
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCase(@Param("name") name: String, pageable: Pageable): Page<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members")
    fun findAllWithMembers(): List<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members",
           countQuery = "SELECT COUNT(t) FROM Team t")
    fun findAllWithMembers(pageable: Pageable): Page<Team>

    // New queries to fetch teams with projects for project count
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects")
    fun findAllWithMembersAndProjects(): List<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects",
           countQuery = "SELECT COUNT(t) FROM Team t")
    fun findAllWithMembersAndProjects(pageable: Pageable): Page<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)")
    fun findByMemberWithProjects(@Param("member") member: User): List<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.members m WHERE m = :member")
    fun findByMemberWithProjects(@Param("member") member: User, pageable: Pageable): Page<Team>

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCaseWithProjects(@Param("name") name: String, pageable: Pageable): Page<Team>
}
