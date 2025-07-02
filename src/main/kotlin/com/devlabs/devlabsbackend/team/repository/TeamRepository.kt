package com.devlabs.devlabsbackend.team.repository

import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import java.util.*

@RepositoryRestResource(path = "teams")
interface TeamRepository : JpaRepository<Team, UUID> {    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)")
    fun findByMemberList(@Param("member") member: User): List<Team>

    @RestResource(path = "byMember")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.members m WHERE m = :member")
    fun findByMember(@Param("member") member: User, pageable: Pageable): Page<Team>    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCaseList(@Param("name") name: String): List<Team>
    
    @RestResource(path = "byNameContaining")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCase(@Param("name") name: String, pageable: Pageable): Page<Team>    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members")
    fun findAllWithMembersList(): List<Team>

    @RestResource(path = "allWithMembers")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members",
           countQuery = "SELECT COUNT(t) FROM Team t")
    fun findAllWithMembers(pageable: Pageable): Page<Team>
    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects")
    fun findAllWithMembersAndProjectsList(): List<Team>

    @RestResource(path = "allWithMembersAndProjects")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects",
           countQuery = "SELECT COUNT(t) FROM Team t")
    fun findAllWithMembersAndProjects(pageable: Pageable): Page<Team>    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)")
    fun findByMemberWithProjectsList(@Param("member") member: User): List<Team>

    @RestResource(path = "byMemberWithProjects")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.members m WHERE m = :member")
    fun findByMemberWithProjects(@Param("member") member: User, pageable: Pageable): Page<Team>

    @RestResource(path = "byNameContainingWithProjects")
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCaseWithProjects(@Param("name") name: String, pageable: Pageable): Page<Team>

    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects " +
           "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND t IN " +
           "(SELECT t2 FROM Team t2 JOIN t2.members m WHERE m = :member)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.members m " +
                       "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND m = :member")
    fun findByNameContainingIgnoreCaseAndMembersContaining(
        @Param("name") name: String, 
        @Param("member") member: User, 
        pageable: Pageable
    ): Page<Team>

    @RestResource(exported = false)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects " +
           "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND t IN " +
           "(SELECT t2 FROM Team t2 JOIN t2.projects p JOIN p.courses c JOIN c.instructors i WHERE i = :instructor)",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.projects p JOIN p.courses c JOIN c.instructors i " +
                       "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) AND i = :instructor")
    fun findByNameContainingIgnoreCaseAndCourseInstructorsContaining(
        @Param("name") name: String, 
        @Param("instructor") instructor: User, 
        pageable: Pageable
    ): Page<Team>

    @RestResource(path = "byIdWithMembers", exported = false)
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :teamId")
    fun findByIdWithMembers(@Param("teamId") teamId: UUID): Team?
    
    @RestResource(path = "byIdWithMembersAndProjects", exported = false)
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members LEFT JOIN FETCH t.projects WHERE t.id = :teamId")
    fun findByIdWithMembersAndProjects(@Param("teamId") teamId: UUID): Team?
}
