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
    fun findProjectsToAutoComplete(): List<Project>    // Find active projects by status (ONGOING and PROPOSED) that have courses
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c WHERE p.status IN :statuses")
    fun findByStatusIn(@Param("statuses") statuses: List<ProjectStatus>): List<Project>
      // Find active projects by semester
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c WHERE p.status IN (com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING, com.devlabs.devlabsbackend.project.domain.ProjectStatus.PROPOSED) AND c.semester.id = :semesterId")
    fun findActiveProjectsBySemester(@Param("semesterId") semesterId: UUID): List<Project>
    
    // Find active projects by batch
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.batches b WHERE p.status IN (com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING, com.devlabs.devlabsbackend.project.domain.ProjectStatus.PROPOSED) AND b.id = :batchId")
    fun findActiveProjectsByBatch(@Param("batchId") batchId: UUID): List<Project>
    
    // Find completed projects for faculty (courses they have taught)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND i = :faculty")
    fun findCompletedProjectsByFaculty(@Param("faculty") faculty: com.devlabs.devlabsbackend.user.domain.User, pageable: Pageable): Page<Project>
    
    // Find completed projects for students (projects they were team members of)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND m = :student")
    fun findCompletedProjectsByStudent(@Param("student") student: com.devlabs.devlabsbackend.user.domain.User, pageable: Pageable): Page<Project>
    
    // Find all completed projects (for admin/manager)
    fun findByStatusOrderByUpdatedAtDesc(status: ProjectStatus, pageable: Pageable): Page<Project>
    
    // Search completed projects for faculty
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND i = :faculty AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchCompletedProjectsByFaculty(@Param("faculty") faculty: com.devlabs.devlabsbackend.user.domain.User, @Param("query") query: String, pageable: Pageable): Page<Project>
    
    // Search completed projects for students
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchCompletedProjectsByStudent(@Param("student") student: com.devlabs.devlabsbackend.user.domain.User, @Param("query") query: String, pageable: Pageable): Page<Project>
    
    // Search all completed projects (for admin/manager)
    @Query("SELECT p FROM Project p WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.updatedAt DESC")
    fun searchCompletedProjects(@Param("query") query: String, pageable: Pageable): Page<Project>
    
    // Search projects by title for faculty (projects from courses they teach)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE i = :instructor AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCaseAndCoursesInstructorsContaining(
        @Param("query") query: String, 
        @Param("instructor") instructor: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>
    
    // Search projects by title for students (projects they are team members of)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCaseAndTeamMembersContaining(
        @Param("query") query: String, 
        @Param("student") student: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>
    
    // Search projects by course and title for students (projects they are team members of in a specific course)
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN p.team t JOIN t.members m WHERE c = :course AND m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCaseAndTeamMembersContaining(
        @Param("course") course: com.devlabs.devlabsbackend.course.domain.Course,
        @Param("query") query: String, 
        @Param("student") student: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>
    
}
