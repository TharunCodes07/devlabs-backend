package com.devlabs.devlabsbackend.team.controller

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.team.domain.DTO.CreateTeamRequest
import com.devlabs.devlabsbackend.team.domain.DTO.TeamResponse
import com.devlabs.devlabsbackend.team.domain.DTO.UpdateTeamRequest
import com.devlabs.devlabsbackend.team.domain.Team
import com.devlabs.devlabsbackend.team.service.TeamService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
) {
    @PostMapping
    fun createTeam(@RequestBody request: CreateTeamRequest): ResponseEntity<Any> {
        return try {
            val team = teamService.createTeam(request, request.creatorId)
            ResponseEntity.status(HttpStatus.CREATED).body(team)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NotFoundException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create team"))
        }
    }

    @GetMapping
    fun getAllTeams(
        @RequestParam(defaultValue = "0") page: Int, 
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.getAllTeams(page, size, sort_by, sort_order)
            ResponseEntity.ok(teams)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get teams"))
        }
    }

    @GetMapping("/{teamId}")
    fun getTeamById(@PathVariable teamId: UUID): ResponseEntity<Any> {
        return try {
            val team = teamService.getTeamById(teamId)
            ResponseEntity.ok(team.toTeamResponse())
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get team"))
        }
    }

    @PutMapping("/{teamId}")
    fun updateTeam(
        @PathVariable teamId: UUID,
        @RequestBody request: UpdateTeamRequest
    ): ResponseEntity<Any> {
        return try {
            val team = teamService.updateTeam(teamId, request)
            ResponseEntity.ok(team.toTeamResponse())
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update team"))
        }
    }

    @DeleteMapping("/{teamId}")
    fun deleteTeam(@PathVariable teamId: UUID): ResponseEntity<Any> {
        return try {
            teamService.deleteTeam(teamId)
            ResponseEntity.noContent().build()
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete team"))
        }
    }      
    
    @GetMapping("/user/{userId}")
    fun getTeamsByUser(
        @PathVariable userId: String, 
        @RequestParam(defaultValue = "0") page: Int, 
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.getTeamsByUser(userId, page, size, sort_by, sort_order)
            ResponseEntity.ok(teams)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get teams: ${e.message}"))
        }
    }

    @GetMapping("/students/search")
    fun searchStudents(@RequestParam query: String): ResponseEntity<Any> {
        return try {
            val students = teamService.searchStudents(query)
            ResponseEntity.ok(students)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search students"))
        }
    }

    @GetMapping("/search/{userId}")
    fun searchTeamsByUser(
        @PathVariable userId: String,
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort_by: String,
        @RequestParam(defaultValue = "asc") sort_order: String
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.searchTeamsByUser(userId, query, page, size, sort_by, sort_order)
            ResponseEntity.ok(teams)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search teams: ${e.message}"))
        }
    }

}

private fun Team.toTeamResponse(): TeamResponse {
    return TeamResponse(
        id =  this.id,
        name = this.name,
        description = this.description,
        members = this.members,
        projectCount = this.projects.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}