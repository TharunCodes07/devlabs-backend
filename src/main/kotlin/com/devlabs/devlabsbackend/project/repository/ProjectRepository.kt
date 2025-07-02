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
    @RestResource(exported = false)
    fun findByTeam(team: Team): List<Project>
    
    @RestResource(path = "byTeam")
    @Query("SELECT p FROM Project p WHERE p.team = :team")
    fun findByTeam(@Param("team") team: Team, pageable: Pageable): Page<Project>
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course")
    fun findByCourse(course: Course): List<Project>
    
    @RestResource(path = "byCourse")
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course")
    fun findByCourse(@Param("course") course: Course, pageable: Pageable): Page<Project>
    
    @Query("SELECT p FROM Project p WHERE p.courses IS EMPTY")
    fun findProjectsWithoutCourse(): List<Project>
    @RestResource(exported = false)
    fun findByStatus(status: ProjectStatus): List<Project>
    
    @RestResource(path = "byStatus")
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    fun findByStatus(@Param("status") status: ProjectStatus, pageable: Pageable): Page<Project>
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE p.team = :team AND c = :course")
    fun findByTeamAndCourse(@Param("team") team: Team, @Param("course") course: Course): List<Project>
    
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND p.status = :status")
    fun findByCourseAndStatus(@Param("course") course: Course, @Param("status") status: ProjectStatus): List<Project>
    @RestResource(exported = false)
    fun findByTitleContainingIgnoreCase(title: String): List<Project>
    
    @RestResource(path = "byTitleContaining")
    @Query("SELECT p FROM Project p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCase(@Param("query") query: String, pageable: Pageable): Page<Project>
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String): List<Project>
    
    @RestResource(path = "byTeamAndTitleContaining")
    @Query("SELECT p FROM Project p WHERE p.team = :team AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTeamAndTitleContainingIgnoreCase(@Param("team") team: Team, @Param("query") query: String, pageable: Pageable): Page<Project>
    @RestResource(exported = false)
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCase(@Param("course") course: Course, @Param("query") query: String): List<Project>
    
    @RestResource(path = "byCourseAndTitleContaining")
    @Query("SELECT p FROM Project p JOIN p.courses c WHERE c = :course AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCase(@Param("course") course: Course, @Param("query") query: String, pageable: Pageable): Page<Project>
      
    @Query("SELECT p FROM Project p WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING AND (p.courses IS EMPTY OR EXISTS (SELECT c FROM p.courses c WHERE c.semester.isActive = false))")
    fun findProjectsToAutoComplete(): List<Project>
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c WHERE p.status IN :statuses")
    fun findByStatusIn(@Param("statuses") statuses: List<ProjectStatus>): List<Project>
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c WHERE p.status IN (com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING, com.devlabs.devlabsbackend.project.domain.ProjectStatus.PROPOSED) AND c.semester.id = :semesterId")
    fun findActiveProjectsBySemester(@Param("semesterId") semesterId: UUID): List<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.batches b WHERE p.status IN (com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING, com.devlabs.devlabsbackend.project.domain.ProjectStatus.PROPOSED) AND b.id = :batchId")
    fun findActiveProjectsByBatch(@Param("batchId") batchId: UUID): List<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND i = :faculty")
    fun findCompletedProjectsByFaculty(@Param("faculty") faculty: com.devlabs.devlabsbackend.user.domain.User, pageable: Pageable): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND m = :student")
    fun findCompletedProjectsByStudent(@Param("student") student: com.devlabs.devlabsbackend.user.domain.User, pageable: Pageable): Page<Project>
    
    fun findByStatusOrderByUpdatedAtDesc(status: ProjectStatus, pageable: Pageable): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND i = :faculty AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchCompletedProjectsByFaculty(@Param("faculty") faculty: com.devlabs.devlabsbackend.user.domain.User, @Param("query") query: String, pageable: Pageable): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchCompletedProjectsByStudent(@Param("student") student: com.devlabs.devlabsbackend.user.domain.User, @Param("query") query: String, pageable: Pageable): Page<Project>
    
    @Query("SELECT p FROM Project p WHERE p.status = com.devlabs.devlabsbackend.project.domain.ProjectStatus.COMPLETED AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.updatedAt DESC")
    fun searchCompletedProjects(@Param("query") query: String, pageable: Pageable): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE i = :instructor AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCaseAndCoursesInstructorsContaining(
        @Param("query") query: String, 
        @Param("instructor") instructor: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.team t JOIN t.members m WHERE m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByTitleContainingIgnoreCaseAndTeamMembersContaining(
        @Param("query") query: String, 
        @Param("student") student: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN p.team t JOIN t.members m WHERE c = :course AND m = :student AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun findByCourseAndTitleContainingIgnoreCaseAndTeamMembersContaining(
        @Param("course") course: com.devlabs.devlabsbackend.course.domain.Course,
        @Param("query") query: String, 
        @Param("student") student: com.devlabs.devlabsbackend.user.domain.User, 
        pageable: Pageable
    ): Page<Project>

    @Query("SELECT DISTINCT p FROM Project p JOIN p.courses c JOIN c.instructors i WHERE i.id = :facultyId AND p.status IN (com.devlabs.devlabsbackend.project.domain.ProjectStatus.PROPOSED, com.devlabs.devlabsbackend.project.domain.ProjectStatus.ONGOING)")
    fun findActiveProjectsByFaculty(
        @Param("facultyId") facultyId: String
    ): List<Project>

    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members
        LEFT JOIN FETCH p.courses c
        LEFT JOIN FETCH c.students
        LEFT JOIN FETCH c.instructors
        LEFT JOIN FETCH p.reviews
        WHERE p.id = :projectId
    """)
    fun findByIdWithRelations(@Param("projectId") projectId: UUID): Project?
    
    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members
        LEFT JOIN FETCH p.courses c
        LEFT JOIN FETCH c.students
        LEFT JOIN FETCH c.instructors
        LEFT JOIN FETCH p.reviews
        WHERE p IN (SELECT proj FROM Project proj JOIN proj.courses course WHERE course = :course)
    """)
    fun findByCourseWithRelations(@Param("course") course: Course, pageable: Pageable): Page<Project>

    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members
        WHERE p.id = :projectId
    """)
    fun findByIdWithTeamAndMembers(@Param("projectId") projectId: UUID): Project?

    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members
        LEFT JOIN FETCH p.courses c
        LEFT JOIN FETCH c.students
        LEFT JOIN FETCH c.instructors
        LEFT JOIN FETCH p.reviews
        WHERE p.team = :team
    """)
    fun findByTeamWithRelations(@Param("team") team: Team, pageable: Pageable): Page<Project>

    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members tm
        LEFT JOIN FETCH p.courses c
        LEFT JOIN FETCH c.students
        LEFT JOIN FETCH c.instructors
        LEFT JOIN FETCH p.reviews
        WHERE t IN (SELECT team FROM Team team JOIN team.members member WHERE member = :user)
        AND :course MEMBER OF p.courses
    """)
    fun findProjectsByUserAndCourseWithRelations(@Param("user") user: com.devlabs.devlabsbackend.user.domain.User, @Param("course") course: Course): List<Project>

    @Query("""
        SELECT DISTINCT p FROM Project p 
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.members
        LEFT JOIN FETCH p.courses c
        LEFT JOIN FETCH c.students
        LEFT JOIN FETCH c.instructors
        LEFT JOIN FETCH p.reviews
        WHERE t IN (SELECT team FROM Team team JOIN team.members member WHERE member = :user)
    """)
    fun findProjectsByUserWithRelations(@Param("user") user: com.devlabs.devlabsbackend.user.domain.User): List<Project>
}
