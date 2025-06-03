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
import java.util.*

@RepositoryRestResource(path = "projects")
interface ProjectRepository : JpaRepository<Project, UUID> {
    
    // Find projects by team
    fun findByTeam(team: Team): List<Project>
    
    // Find projects by team with pagination
    @Query("SELECT p FROM Project p WHERE p.team = :team")
    fun findByTeam(@Param("team") team: Team, pageable: Pageable): Page<Project>
      
    // Find projects by course
    fun findByCourse(course: Course): List<Project>
    
    // Find projects by course with pagination
    @Query("SELECT p FROM Project p WHERE p.course = :course")
    fun findByCourse(@Param("course") course: Course, pageable: Pageable): Page<Project>
    
    // Find projects with no course
    @Query("SELECT p FROM Project p WHERE p.course IS NULL")
    fun findProjectsWithoutCourse(): List<Project>
    
    // Find projects by status
    fun findByStatus(status: ProjectStatus): List<Project>
    
    // Find projects by status with pagination
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    fun findByStatus(@Param("status") status: ProjectStatus, pageable: Pageable): Page<Project>
    
    // Find projects by team and course
    fun findByTeamAndCourse(team: Team, course: Course): List<Project>
    
    // Find projects by course and status    
    fun findByCourseAndStatus(course: Course, status: ProjectStatus): List<Project>

    // Find projects by title containing (case-insensitive)
    fun findByTitleContainingIgnoreCase(title: String): List<Project>
    
    // Find projects by title containing (case-insensitive) with pagination
    @Query("SELECT p FROM Project p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Project>
    
    // Find projects by team and with title containing (case-insensitive)
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String): List<Project>
    
    // Find projects by team and with title containing (case-insensitive) with pagination
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String, pageable: Pageable): Page<Project>
      
    // Find projects that can be auto-completed (ongoing projects in inactive semesters)
    @Query("SELECT p FROM Project p WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING AND (p.course IS NULL OR p.course.semester.isActive = false)")
    fun findProjectsToAutoComplete(): List<Project>

}
