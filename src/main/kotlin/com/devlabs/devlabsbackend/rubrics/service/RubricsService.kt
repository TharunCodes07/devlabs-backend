package com.devlabs.devlabsbackend.rubrics.service

import com.devlabs.devlabsbackend.core.config.CacheConfig
import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.criterion.service.toCriterionResponse
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreatedByResponse
import com.devlabs.devlabsbackend.rubrics.domain.dto.RubricsResponse
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RubricsService(
    private val rubricsRepository: RubricsRepository,
    private val criterionRepository: CriterionRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    @Cacheable(value = [CacheConfig.RUBRICS_CACHE], key = "'user:' + #userId")
    fun getRubricsByUser(userId: String): List<RubricsResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val userRubrics = rubricsRepository.findByCreatedBy(user)

        val sharedRubrics = rubricsRepository.findByIsSharedTrue()

        val combinedRubrics = (userRubrics + sharedRubrics.filter { shared ->
            userRubrics.none { it.id == shared.id }
        }).distinctBy { it.id }

        return combinedRubrics.map { it.toRubricsResponse() }
    }


    @Transactional(readOnly = true)
    @Cacheable(value = [CacheConfig.RUBRICS_CACHE], key = "'id:' + #id")
    fun getRubricsById(id: UUID): RubricsResponse {
        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }
        return rubrics.toRubricsResponse()
    }

    @Transactional
    @CacheEvict(value = [CacheConfig.RUBRICS_CACHE], allEntries = true)
    fun createRubrics(request: CreateRubricsRequest): RubricsResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            NotFoundException("User with id ${request.userId} not found")
        }
        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw ForbiddenException("Only admin, manager, or faculty can create rubrics")
        }
        if (request.isShared && user.role != Role.ADMIN && user.role != Role.MANAGER) {
            throw ForbiddenException("Only admin or manager can create shared rubrics")
        }

        val rubrics = Rubrics(
            name = request.name,
            createdBy = user,
            isShared = request.isShared
        )

        val savedRubrics = rubricsRepository.save(rubrics)

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

    @Transactional
    @CacheEvict(value = [CacheConfig.RUBRICS_CACHE], allEntries = true)
    fun updateRubrics(id: UUID, request: CreateRubricsRequest): RubricsResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            NotFoundException("User with id ${request.userId} not found")
        }

        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }

        when (user.role) {
            Role.FACULTY -> {
                if (rubrics.createdBy.id != user.id) {
                    throw ForbiddenException("Faculty can only edit rubrics they created")
                }
                if (request.isShared != rubrics.isShared) {
                    throw ForbiddenException("Faculty cannot change shared status of rubrics")
                }
            }
            else -> throw ForbiddenException("You don't have permission to update rubrics")
        }
        rubrics.name = request.name

        if (user.role == Role.ADMIN || user.role == Role.MANAGER) {
            rubrics.isShared = request.isShared
        }

        val existingCriteria = criterionRepository.findAllByRubrics(rubrics)
        existingCriteria.forEach {
            it.rubrics = null
            criterionRepository.delete(it)
        }

        rubrics.criteria.clear()

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

    @Transactional
    @CacheEvict(value = [CacheConfig.RUBRICS_CACHE], allEntries = true)
    fun deleteRubrics(id: UUID, userId: String) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val rubrics = rubricsRepository.findById(id).orElseThrow {
            NotFoundException("Rubrics with id $id not found")
        }
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> { }
            Role.FACULTY -> {
                if (rubrics.createdBy.id != user.id) {
                    throw ForbiddenException("Faculty can only delete rubrics they created")
                }
            }
            else -> throw ForbiddenException("You don't have permission to delete rubrics")
        }

        val criteria = criterionRepository.findAllByRubrics(rubrics)
        criteria.forEach {
            it.rubrics = null
            criterionRepository.delete(it)
        }

        rubricsRepository.delete(rubrics)
    }

    @Transactional(readOnly = true)
    @Cacheable(value = [CacheConfig.RUBRICS_CACHE], key = "'all'")
    fun getAllRubrics(): List<RubricsResponse> {
        return rubricsRepository.findAll().map { it.toRubricsResponse() }
    }

}

fun Rubrics.toRubricsResponse(): RubricsResponse {
    val criteriaResponses = this.criteria.map { criterion ->
        criterion.toCriterionResponse()
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
    )
}