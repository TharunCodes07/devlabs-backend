package com.devlabs.devlabsbackend.team.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.team.domain.DTO.CreateTeamRequest
import com.devlabs.devlabsbackend.team.domain.DTO.TeamResponse
import com.devlabs.devlabsbackend.team.domain.DTO.UpdateTeamRequest
import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import com.devlabs.devlabsbackend.user.service.toUserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val current_page: Int,
    val per_page: Int,
    val total_pages: Int,
    val total_count: Int
)

@Service
@Transactional
class TeamService(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {
    fun createTeam(teamData: CreateTeamRequest, creatorId: String): Team {
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }

        val team = Team(
            name = teamData.name,
            description = teamData.description
        )

        team.members.add(creator)

        if (teamData.memberIds.isNotEmpty()) {
            val members = userRepository.findAllById(teamData.memberIds)

            if (members.size != teamData.memberIds.size) {
                throw NotFoundException("Some members could not be found")
            }

            val nonStudents = members.filter { it.role != Role.STUDENT }
            if (nonStudents.isNotEmpty()) {
                throw IllegalArgumentException("Only students can join teams")
            }
            members.forEach { member ->
                if (member.id != creatorId) {
                    team.members.add(member)
                }
            }
        }
        return teamRepository.save(team)
    }

    fun getAllTeams(page: Int = 0, size: Int = 10): PaginatedResponse<TeamResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val teamsPage: Page<Team> = teamRepository.findAllWithMembersAndProjects(pageable)

        return PaginatedResponse(
            data = teamsPage.content.map { team -> team.toTeamResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = teamsPage.totalPages,
                total_count = teamsPage.totalElements.toInt()
            )
        )
    }    fun getTeamsByUser(userId: String, page: Int = 0, size: Int = 10, sortBy: String = "name", sortOrder: String = "asc"): PaginatedResponse<TeamResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        
        val teamsPage: Page<Team> = teamRepository.findByMemberWithProjects(user, pageable)

        return PaginatedResponse(
            data = teamsPage.content.map { team -> team.toTeamResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = teamsPage.totalPages,
                total_count = teamsPage.totalElements.toInt()
            )
        )
    }

    fun getTeamById(teamId: UUID): Team {
        val team = teamRepository.findById(teamId).orElseThrow {
            NotFoundException("Team with id $teamId not found")
        }
        team.members.size
        team.projects.size

        return team
    }

    fun updateTeam(teamId: UUID, updateData: UpdateTeamRequest): Team {
        val team = getTeamById(teamId)

        updateData.name?.let { team.name = it }
        updateData.description?.let { team.description = it }

        if (updateData.memberIds != null) {
            val newMembers = userRepository.findAllById(updateData.memberIds)

            if (newMembers.size != updateData.memberIds.size) {
                throw NotFoundException("Some member IDs could not be found")
            }

            val nonStudents = newMembers.filter { it.role != Role.STUDENT }
            if (nonStudents.isNotEmpty()) {
                throw IllegalArgumentException("Only students can be team members")
            }

            team.members.clear()
            team.members.addAll(newMembers)
        }

        team.updatedAt = java.sql.Timestamp(java.time.Instant.now().toEpochMilli())

        return teamRepository.save(team)
    }


    fun deleteTeam(teamId: UUID) {
        if (!teamRepository.existsById(teamId)) {
            throw NotFoundException("Team with id $teamId not found")
        }
        teamRepository.deleteById(teamId)
    }

    fun searchTeamsByUser(userId: String, query: String, page: Int = 0, size: Int = 10): PaginatedResponse<TeamResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val pageable: Pageable = PageRequest.of(page, size)

        val teamsPage: Page<Team> = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {

                teamRepository.findByNameContainingIgnoreCaseWithProjects(query, pageable)
            }
            Role.FACULTY -> {
                teamRepository.findByNameContainingIgnoreCaseAndCourseInstructorsContaining(query, user, pageable)
            }
            Role.STUDENT -> {
                teamRepository.findByNameContainingIgnoreCaseAndMembersContaining(query, user, pageable)
            }
            else -> {
                Page.empty(pageable)
            }
        }



        return PaginatedResponse(
            data = teamsPage.content.map { team -> team.toTeamResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = teamsPage.totalPages,
                total_count = teamsPage.totalElements.toInt()
            )
        )
    }

    fun searchStudents(query: String): List<com.devlabs.devlabsbackend.user.domain.DTO.UserResponse> {
        val students = userRepository.findByNameOrEmailContainingIgnoreCase(query)
            .filter { it.role == Role.STUDENT }
        return students.map { it.toUserResponse() }
    }


}

fun Team.toTeamResponse(): TeamResponse {
    return TeamResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        members = this.members,
        projectCount = this.projects.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}