package com.devlabs.devlabsbackend.criterion.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class CriterionServiceTest {
    
    private lateinit var criterionService: CriterionService
    private lateinit var criterionRepository: CriterionRepository
    private lateinit var rubricsRepository: RubricsRepository
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setUp() {
        criterionRepository = mock(CriterionRepository::class.java)
        rubricsRepository = mock(RubricsRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        
        criterionService = CriterionService(
            criterionRepository,
            rubricsRepository,
            userRepository
        )
    }
    
    @Test
    fun `getCriterionById returns criterion when found`() {
        // Given
        val criterionId = UUID.randomUUID()
        val rubrics = Rubrics(UUID.randomUUID(), "Test Rubrics")
        val criterion = Criterion(
            criterionId,
            "Test Criterion",
            "Description",
            5.0f,
            true,
            rubrics
        )
        
        `when`(criterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion))
        
        // When
        val result = criterionService.getCriterionById(criterionId)
        
        // Then
        assertEquals(criterionId, result.id)
        assertEquals("Test Criterion", result.name)
        assertEquals("Description", result.description)
        assertEquals(5.0f, result.maxScore)
        assertEquals(true, result.isCommon)
        verify(criterionRepository).findById(criterionId)
    }
    
    @Test
    fun `getCriterionById throws NotFoundException when not found`() {
        // Given
        val criterionId = UUID.randomUUID()
        `when`(criterionRepository.findById(criterionId)).thenReturn(Optional.empty())
        
        // When/Then
        val exception = assertThrows<NotFoundException> {
            criterionService.getCriterionById(criterionId)
        }
        
        assertEquals("Criterion with id $criterionId not found", exception.message)
        verify(criterionRepository).findById(criterionId)
    }
    
    @Test
    fun `getCriteriaByRubricsId returns list of criteria`() {
        // Given
        val rubricsId = UUID.randomUUID()
        val rubrics = Rubrics(rubricsId, "Test Rubrics")
        
        val criterion1 = Criterion(
            UUID.randomUUID(),
            "Criterion 1",
            "Description 1",
            10.0f,
            true,
            rubrics
        )
        
        val criterion2 = Criterion(
            UUID.randomUUID(),
            "Criterion 2",
            "Description 2",
            5.0f,
            false,
            rubrics
        )
        
        `when`(rubricsRepository.findById(rubricsId)).thenReturn(Optional.of(rubrics))
        `when`(criterionRepository.findAllByRubrics(rubrics)).thenReturn(listOf(criterion1, criterion2))
        
        // When
        val result = criterionService.getCriteriaByRubricsId(rubricsId)
        
        // Then
        assertEquals(2, result.size)
        assertEquals("Criterion 1", result[0].name)
        assertEquals("Criterion 2", result[1].name)
        verify(rubricsRepository).findById(rubricsId)
        verify(criterionRepository).findAllByRubrics(rubrics)
    }
    
    @Test
    fun `deleteCriterion succeeds for admin user`() {
        // Given
        val userId = UUID.randomUUID()
        val criterionId = UUID.randomUUID()
        
        val admin = User().apply { 
            id = userId
            role = Role.ADMIN
        }
        
        val rubrics = Rubrics(UUID.randomUUID(), "Test Rubrics")
        val criterion = Criterion(
            criterionId,
            "Test Criterion",
            "Description",
            5.0f,
            true,
            rubrics
        )
        rubrics.criteria.add(criterion)
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(admin))
        `when`(criterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion))
        
        // When
        criterionService.deleteCriterion(criterionId, userId)
        
        // Then
        verify(userRepository).findById(userId)
        verify(criterionRepository).findById(criterionId)
        verify(criterionRepository).save(criterion)
        verify(criterionRepository).delete(criterion)
        assertEquals(0, rubrics.criteria.size)
        assertEquals(null, criterion.rubrics)
    }
    
    @Test
    fun `deleteCriterion throws ForbiddenException for non-admin user`() {
        // Given
        val userId = UUID.randomUUID()
        val criterionId = UUID.randomUUID()
        
        val faculty = User().apply { 
            id = userId
            role = Role.FACULTY
        }
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(faculty))
        
        // When/Then
        val exception = assertThrows<ForbiddenException> {
            criterionService.deleteCriterion(criterionId, userId)
        }
        
        assertEquals("Only admin or manager can delete criterion", exception.message)
        verify(userRepository).findById(userId)
        verifyNoInteractions(criterionRepository)
    }
}
