package com.devlabs.devlabsbackend.team.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.team.domain.DTO.CreateTeamRequest
import com.devlabs.devlabsbackend.team.domain.DTO.TeamResponse
import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
    fun createTeam(teamData: CreateTeamRequest, creatorId: UUID): Team {
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }

        if (creator.role != Role.STUDENT) {
            throw IllegalArgumentException("Only students can create teams")
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
    }    fun getAllTeams(page: Int = 0, size: Int = 10): PaginatedResponse<TeamResponse> {
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
    }

    fun getTeamsByUser(userId: UUID, page: Int = 0, size: Int = 10): PaginatedResponse<TeamResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        val pageable: Pageable = PageRequest.of(page, size)
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

    fun searchTeams(query: String, page: Int = 0, size: Int = 10): PaginatedResponse<TeamResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val teamsPage: Page<Team> = teamRepository.findByNameContainingIgnoreCaseWithProjects(query, pageable)
        
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

    private fun Team.toTeamResponse(): TeamResponse {
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
}
