package com.devlabs.devlabsbackend.criterion.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.criterion.service.toCriterionResponse
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CriterionService(
    private val criterionRepository: CriterionRepository,
    private val rubricsRepository: RubricsRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getCriterionById(id: UUID): CriterionResponse {
        val criterion = criterionRepository.findById(id).orElseThrow {
            NotFoundException("Criterion with id $id not found")
        }
        
        return criterion.toCriterionResponse()
    }
    
    @Transactional(readOnly = true)
    fun getCriteriaByRubricsId(rubricsId: UUID): List<CriterionResponse> {
        val rubrics = rubricsRepository.findById(rubricsId).orElseThrow {
            NotFoundException("Rubrics with id $rubricsId not found")
        }
        
        val criteria = criterionRepository.findAllByRubrics(rubrics)
        return criteria.map { it.toCriterionResponse() }
    }
    
    @Transactional
    fun deleteCriterion(id: UUID, userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        // Only Admin and Manager can delete criterion
        if (user.role != Role.ADMIN && user.role != Role.MANAGER) {
            throw ForbiddenException("Only admin or manager can delete criterion")
        }
        
        val criterion = criterionRepository.findById(id).orElseThrow {
            NotFoundException("Criterion with id $id not found")
        }
        
        // Remove the criterion from the rubrics
        criterion.rubrics?.let { rubrics ->
            rubrics.criteria.remove(criterion)
        }
        
        // Set rubrics to null to avoid cascade issues
        criterion.rubrics = null
        
        // Save the updated criterion and then delete it
        criterionRepository.save(criterion)
        criterionRepository.delete(criterion)
    }
}
