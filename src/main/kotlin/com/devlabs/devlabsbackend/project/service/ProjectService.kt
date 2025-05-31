package com.devlabs.devlabsbackend.project.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.project.domain.DTO.CreateProjectRequest
import com.devlabs.devlabsbackend.project.domain.DTO.UpdateProjectRequest
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val teamRepository: TeamRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {

    fun createProject(projectData: CreateProjectRequest, requesterId: UUID): Project {
        val team = teamRepository.findById(projectData.teamId).orElseThrow {
            NotFoundException("Team with id ${projectData.teamId} not found")
        }
        
        val course = courseRepository.findById(projectData.courseId).orElseThrow {
            NotFoundException("Course with id ${projectData.courseId} not found")
        }
        
        val requester = userRepository.findById(requesterId).orElseThrow {
            NotFoundException("User with id $requesterId not found")
        }

        if (!team.isMember(requester)) {
            throw IllegalArgumentException("Only team members can create projects")
        }

        val teamMemberIds = team.members.map { it.id }
        val enrolledStudents = course.students.map { it.id }
        if (!teamMemberIds.any { it in enrolledStudents }) {
            throw IllegalArgumentException("Team is not enrolled in this course")
        }
          val project = Project(
            title = projectData.title,
            description = projectData.description,
            objectives = projectData.objectives,
            team = team,
            course = course
        )
        return projectRepository.save(project)
    }

    fun updateProject(projectId: UUID, updateData: UpdateProjectRequest, requesterId: UUID): Project {
        val project = getProjectById(projectId)
        val requester = userRepository.findById(requesterId).orElseThrow {
            NotFoundException("User with id $requesterId not found")
        }

        if (!project.team.isMember(requester)) {
            throw IllegalArgumentException("Only team members can update projects")
        }
        if (project.status !in listOf(ProjectStatus.PROPOSED, ProjectStatus.REJECTED)) {
            throw IllegalArgumentException("Project cannot be edited in current status: ${project.status}")
        }
        updateData.title?.let { project.title = it }
        updateData.description?.let { project.description = it }
        updateData.objectives?.let { project.objectives = it }

        project.updatedAt = Timestamp.from(Instant.now())
        
        return projectRepository.save(project)
    }

    fun approveProject(projectId: UUID, instructorId: UUID): Project {
        val project = getProjectById(projectId)
        val instructor = userRepository.findById(instructorId).orElseThrow {
            NotFoundException("User with id $instructorId not found")
        }
        
        if (!project.course.instructors.contains(instructor)) {
            throw IllegalArgumentException("Only course instructors can approve projects")
        }
        
        if (project.status != ProjectStatus.PROPOSED) {
            throw IllegalArgumentException("Project cannot be approved in current status: ${project.status}")
        }
        
        // Automatically start the project when approved
        project.status = ProjectStatus.ONGOING
        project.updatedAt = Timestamp.from(Instant.now())
          return projectRepository.save(project)
    }
    
    fun rejectProject(projectId: UUID, instructorId: UUID, reason: String?): Project {
        val project = getProjectById(projectId)
        val instructor = userRepository.findById(instructorId).orElseThrow {
            NotFoundException("User with id $instructorId not found")
        }

        if (!project.course.instructors.contains(instructor)) {
            throw IllegalArgumentException("Only course instructors can reject projects")
        }
        if (project.status != ProjectStatus.PROPOSED) {
            throw IllegalArgumentException("Project cannot be rejected in current status: ${project.status}")
        }
        project.status = ProjectStatus.REJECTED
        project.updatedAt = Timestamp.from(Instant.now())
        
        return projectRepository.save(project)
    }

    fun completeProject(projectId: UUID): Project {
        val project = getProjectById(projectId)
        if (project.status != ProjectStatus.ONGOING) {
            throw IllegalArgumentException("Project cannot be completed in current status: ${project.status}")
        }
        project.status = ProjectStatus.COMPLETED
        project.updatedAt = Timestamp.from(Instant.now())
        return projectRepository.save(project)
    }

    fun autoCompleteProjectsForInactiveCourses(): List<Project> {
        val projectsToComplete = projectRepository.findProjectsToAutoComplete()
        return projectsToComplete.map { project ->
            project.status = ProjectStatus.COMPLETED
            project.updatedAt = Timestamp.from(Instant.now())
            projectRepository.save(project)
        }
    }

    fun getProjectsByTeam(teamId: UUID): List<Project> {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }
        return projectRepository.findByTeam(team)
    }

    fun getProjectsByCourse(courseId: UUID): List<Project> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        return projectRepository.findByCourse(course)
    }

    fun getProjectsForUser(userId: UUID): List<Project> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
          // Get all teams where user is a member
        val teams = teamRepository.findByMemberList(user)
        
        // Get all projects for these teams
        return teams.flatMap { team ->
            projectRepository.findByTeam(team)
        }
    }

    fun searchProjects(query: String): List<Project> {
        return projectRepository.findByTitleContainingIgnoreCase(query)
    }    fun getProjectsNeedingApproval(): List<Project> {
        return projectRepository.findByStatus(ProjectStatus.PROPOSED)
    }
    
    fun getOngoingProjects(): List<Project> {
        return projectRepository.findByStatus(ProjectStatus.ONGOING)
    }

    private fun getProjectById(projectId: UUID): Project {
        return projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
    }
}
