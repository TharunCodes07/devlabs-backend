package com.devlabs.devlabsbackend.rubrics.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateCriterionRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreatedByResponse
import com.devlabs.devlabsbackend.rubrics.domain.dto.RubricsResponse
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RubricsService(
    private val rubricsRepository: RubricsRepository,
    private val criterionRepository: CriterionRepository,
    private val userRepository: UserRepository
) {    // Extension function to convert Rubrics to RubricsResponse
    fun Rubrics.toRubricsResponse(): RubricsResponse {
        val criteriaResponses = this.criteria.map { criterion ->
            CriterionResponse(
                id = criterion.id!!,
                name = criterion.name,
                description = criterion.description,
                maxScore = criterion.maxScore,
                isCommon = criterion.isCommon
            )
        }
          return RubricsResponse(
            id = this.id!!,
            name = this.name,
            createdBy = CreatedByResponse(
                id = this.createdBy.id!!,
                name = this.createdBy.name,
                email = this.createdBy.email,
                role = this.createdBy.role.name
            ),
            createdAt = this.createdAt,
            criteria = criteriaResponses,
            isShared = this.isShared
        )    }    // Get rubrics by user ID - returns rubrics created by the user and all shared rubrics
    // Used for both displaying user's rubrics and for review creation
    @Transactional
    fun getRubricsByUser(userId: UUID): List<RubricsResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        // Get rubrics created by the user
        val userRubrics = rubricsRepository.findByCreatedBy(user)
        
        // Get all shared rubrics
        val sharedRubrics = rubricsRepository.findByIsSharedTrue()
        
        // Combine user's rubrics and shared rubrics, removing duplicates
        val combinedRubrics = (userRubrics + sharedRubrics.filter { shared -> 
            userRubrics.none { it.id == shared.id } 
        }).distinctBy { it.id }
        
        return combinedRubrics.map { it.toRubricsResponse() }
    }
    
    // Get rubrics by ID - for when a specific rubric is needed
    @Transactional
    fun getRubricsById(id: UUID): RubricsResponse {
        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }
        return rubrics.toRubricsResponse()
    }    // Get all rubrics - replaces getAllActiveRubrics
    @Transactional
    fun getAllRubrics(): List<RubricsResponse> {
        return rubricsRepository.findAll().map { it.toRubricsResponse() }
    }
    
    // Create new rubrics - simplified version
    @Transactional
    fun createRubrics(request: CreateRubricsRequest): RubricsResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            NotFoundException("User with id ${request.userId} not found")
        }
          // Only Admin, Manager, and Faculty can create rubrics
        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw ForbiddenException("Only admin, manager, or faculty can create rubrics")
        }
        
        // Only Admin and Manager can create shared rubrics
        if (request.isShared && user.role != Role.ADMIN && user.role != Role.MANAGER) {
            throw ForbiddenException("Only admin or manager can create shared rubrics")
        }
        
        val rubrics = Rubrics(
            name = request.name,
            createdBy = user,
            isShared = request.isShared
        )
        
        val savedRubrics = rubricsRepository.save(rubrics)
        
        // Create criteria if provided
        request.criteria.forEach { criterionRequest ->
            val criterion = Criterion(
                name = criterionRequest.name,
                description = criterionRequest.description,
                maxScore = criterionRequest.maxScore,
                isCommon = criterionRequest.isCommon,
                rubrics = savedRubrics
            )
            
            val savedCriterion = criterionRepository.save(criterion)
            savedRubrics.criteria.add(savedCriterion)
        }
        
        return savedRubrics.toRubricsResponse()
    }
    
    // Update an existing Rubric - simplified version
    @Transactional
    fun updateRubrics(id: UUID, request: CreateRubricsRequest): RubricsResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            NotFoundException("User with id ${request.userId} not found")
        }
        
        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }
          // Check permissions - Admin/Manager can edit all, Faculty can only edit their own
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> { /* Allow all */ }
            Role.FACULTY -> {
                if (rubrics.createdBy.id != user.id) {
                    throw ForbiddenException("Faculty can only edit rubrics they created")
                }
                // Faculty cannot change shared status
                if (request.isShared != rubrics.isShared) {
                    throw ForbiddenException("Faculty cannot change shared status of rubrics")
                }
            }
            else -> throw ForbiddenException("You don't have permission to update rubrics")
        }
        
        // Update basic properties
        rubrics.name = request.name
        
        // Only Admin and Manager can update shared status
        if (user.role == Role.ADMIN || user.role == Role.MANAGER) {
            rubrics.isShared = request.isShared
        }
        
        // Clear existing criteria and add new ones
        val existingCriteria = criterionRepository.findAllByRubrics(rubrics)
        existingCriteria.forEach { 
            it.rubrics = null
            criterionRepository.delete(it)
        }
        
        rubrics.criteria.clear()
        
        // Add new criteria
        request.criteria.forEach { criterionRequest ->
            val criterion = Criterion(
                name = criterionRequest.name,
                description = criterionRequest.description,
                maxScore = criterionRequest.maxScore,
                isCommon = criterionRequest.isCommon,
                rubrics = rubrics
            )
            
            val savedCriterion = criterionRepository.save(criterion)
            rubrics.criteria.add(savedCriterion)
        }
        
        return rubricsRepository.save(rubrics).toRubricsResponse()
    }
    
    // Delete a rubrics - might be needed in some cases
    @Transactional
    fun deleteRubrics(id: UUID, userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }
        
        // Check permissions - Admin/Manager can delete all, Faculty can only delete their own
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> { /* Allow all */ }
            Role.FACULTY -> {
                if (rubrics.createdBy.id != user.id) {
                    throw ForbiddenException("Faculty can only delete rubrics they created")
                }
            }
            else -> throw ForbiddenException("You don't have permission to delete rubrics")
        }
        
        // First remove all criteria
        val criteria = criterionRepository.findAllByRubrics(rubrics)
        criteria.forEach { 
            it.rubrics = null
            criterionRepository.delete(it)
        }
        
        // Then delete the rubrics
        rubricsRepository.delete(rubrics)
    }
}