package com.devlabs.devlabsbackend.project.repository

import com.devlabs.devlabsbackend.course.domain.Course
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.team.domain.Team
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import java.util.*

@RepositoryRestResource(path = "projects")
interface ProjectRepository : JpaRepository<Project, UUID> {
      // Find projects by team
    @RestResource(exported = false)
    fun findByTeam(team: Team): List<Project>
    
    // Find projects by team with pagination
    @RestResource(path = "byTeam")
    @Query("SELECT p FROM Project p WHERE p.team = :team")
    fun findByTeam(@Param("team") team: Team, pageable: Pageable): Page<Project>    // Find projects by course
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course")
    fun findByCourse(course: Course): List<Project>
    
    // Find projects by course with pagination
    @RestResource(path = "byCourse")
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course")
    fun findByCourse(@Param("course") course: Course, pageable: Pageable): Page<Project>
    
    // Find projects with no courses
    @Query("SELECT p FROM Project p WHERE p.courses IS EMPTY")
    fun findProjectsWithoutCourse(): List<Project>
      // Find projects by status
    @RestResource(exported = false)
    fun findByStatus(status: ProjectStatus): List<Project>
    
    // Find projects by status with pagination
    @RestResource(path = "byStatus")
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    fun findByStatus(@Param("status") status: ProjectStatus, pageable: Pageable): Page<Project>
      // Find projects by team and course
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE p.team = :team AND c = :course")
    fun findByTeamAndCourse(@Param("team") team: Team, @Param("course") course: Course): List<Project>
    
    // Find projects by course and status    
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND p.status = :status")
    fun findByCourseAndStatus(@Param("course") course: Course, @Param("status") status: ProjectStatus): List<Project>// Find projects by title containing (case-insensitive)
    @RestResource(exported = false)
    fun findByTitleContainingIgnoreCase(title: String): List<Project>
    
    // Find projects by title containing (case-insensitive) with pagination
    @RestResource(path = "byTitleContaining")
    @Query("SELECT p FROM Project p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Project>
      // Find projects by team and with title containing (case-insensitive)
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String): List<Project>
      // Find projects by team and with title containing (case-insensitive) with pagination
    @RestResource(path = "byTeamAndTitleContaining")
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String, pageable: Pageable): Page<Project>
      // Find projects by course and with title containing (case-insensitive)
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCase(@Param("course") course: Course, @Param("query") query: String): List<Project>
    
    // Find projects by course and with title containing (case-insensitive) with pagination
    @RestResource(path = "byCourseAndTitleContaining")
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCase(@Param("course") course: Course, @Param("query") query: String, pageable: Pageable): Page<Project>
      
    // Find projects that can be auto-completed (ongoing projects in inactive semesters)
    @Query("SELECT p FROM Project p WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING AND (p.courses IS EMPTY OR EXISTS (SELECT c FROM p.courses c WHERE c.semester.isActive = false))")
    fun findProjectsToAutoComplete(): List<Project>

}
