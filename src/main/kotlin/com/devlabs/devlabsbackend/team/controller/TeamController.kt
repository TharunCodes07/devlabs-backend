package com.devlabs.devlabsbackend.team.controller

import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.team.domain.DTO.CreateTeamRequest
import com.devlabs.devlabsbackend.team.service.TeamService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
)
{
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
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.getAllTeams(page, size)
            ResponseEntity.ok(teams)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get teams"))
        }
    }

    @GetMapping("/user/{userId}")
    fun getTeamsByUser(
        @PathVariable userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.getTeamsByUser(userId, page, size)
            ResponseEntity.ok(teams)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get teams"))
        }
    }

    @GetMapping("/search")
    fun searchTeams(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        return try {
            val teams = teamService.searchTeams(query, page, size)
            ResponseEntity.ok(teams)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to search teams"))
        }
    }
}