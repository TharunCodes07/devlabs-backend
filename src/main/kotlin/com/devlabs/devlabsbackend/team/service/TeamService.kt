package com.devlabs.devlabsbackend.team.service

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.team.domain.DTO.CreateTeamRequest
import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.team.repository.TeamRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TeamService(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {    fun createTeam(teamData: CreateTeamRequest, creatorId: UUID): Team {
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
    }

    fun getTeamsByUser(userId: UUID): List<Team> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        return teamRepository.findByMember(user)
    }

    fun searchTeams(query: String): List<Team> {
        return teamRepository.findByNameContainingIgnoreCase(query)
    }

}
