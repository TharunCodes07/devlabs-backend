package com.devlabs.devlabsbackend.rubrics.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateCriterionRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.UpdateCriterionRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.UpdateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class RubricsServiceTest {
    
    private lateinit var rubricsService: RubricsService
    private lateinit var rubricsRepository: RubricsRepository
    private lateinit var criterionRepository: CriterionRepository
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setUp() {
        rubricsRepository = mock(RubricsRepository::class.java)
        criterionRepository = mock(CriterionRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        
        rubricsService = RubricsService(
            rubricsRepository,
            criterionRepository,
            userRepository
        )
    }
    
    @Test
    fun `getAllRubrics returns paged rubrics`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val rubrics1 = Rubrics(UUID.randomUUID(), "Rubrics 1")
        val rubrics2 = Rubrics(UUID.randomUUID(), "Rubrics 2")
        val rubricsPage = PageImpl(listOf(rubrics1, rubrics2), pageable, 2)
        
        `when`(rubricsRepository.findAll(pageable)).thenReturn(rubricsPage)
        
        // When
        val result = rubricsService.getAllRubrics(pageable)
        
        // Then
        assertEquals(2, result.totalElements)
        assertEquals("Rubrics 1", result.content[0].name)
        assertEquals("Rubrics 2", result.content[1].name)
        verify(rubricsRepository).findAll(pageable)
    }
    
    @Test
    fun `getRubricsById returns rubrics when found`() {
        // Given
        val rubricsId = UUID.randomUUID()
        val rubrics = Rubrics(rubricsId, "Test Rubrics")
        val criterion = Criterion(
            UUID.randomUUID(),
            "Test Criterion",
            "Description",
            5.0f,
            true,
            rubrics
        )
        rubrics.criteria.add(criterion)
        
        `when`(rubricsRepository.findById(rubricsId)).thenReturn(Optional.of(rubrics))
        
        // When
        val result = rubricsService.getRubricsById(rubricsId)
        
        // Then
        assertEquals(rubricsId, result.id)
        assertEquals("Test Rubrics", result.name)
        assertEquals(1, result.criteria.size)
        assertEquals("Test Criterion", result.criteria[0].name)
        verify(rubricsRepository).findById(rubricsId)
    }
    
    @Test
    fun `getRubricsById throws NotFoundException when not found`() {
        // Given
        val rubricsId = UUID.randomUUID()
        `when`(rubricsRepository.findById(rubricsId)).thenReturn(Optional.empty())
        
        // When/Then
        val exception = assertThrows<NotFoundException> {
            rubricsService.getRubricsById(rubricsId)
        }
        
        assertEquals("Rubrics with id $rubricsId not found", exception.message)
        verify(rubricsRepository).findById(rubricsId)
    }
    
    @Test
    fun `createRubrics succeeds for admin user`() {
        // Given
        val userId = UUID.randomUUID()
        val admin = User().apply { 
            id = userId
            role = Role.ADMIN
        }
        
        val createRequest = CreateRubricsRequest(
            name = "New Rubrics",
            criteria = listOf(
                CreateCriterionRequest(
                    name = "Criterion 1",
                    description = "Description 1",
                    maxScore = 10.0f,
                    isCommon = true
                )
            )
        )
        
        val savedRubrics = Rubrics(UUID.randomUUID(), "New Rubrics")
        val savedCriterion = Criterion(
            UUID.randomUUID(),
            "Criterion 1",
            "Description 1",
            10.0f,
            true,
            savedRubrics
        )
        savedRubrics.criteria.add(savedCriterion)
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(admin))
        `when`(rubricsRepository.save(any(Rubrics::class.java))).thenReturn(savedRubrics)
        `when`(criterionRepository.save(any(Criterion::class.java))).thenReturn(savedCriterion)
        
        // When
        val result = rubricsService.createRubrics(createRequest, userId)
        
        // Then
        assertEquals("New Rubrics", result.name)
        assertEquals(1, result.criteria.size)
        assertEquals("Criterion 1", result.criteria[0].name)
        verify(userRepository).findById(userId)
        verify(rubricsRepository).save(any(Rubrics::class.java))
        verify(criterionRepository).save(any(Criterion::class.java))
    }
    
    @Test
    fun `createRubrics throws ForbiddenException for non-admin user`() {
        // Given
        val userId = UUID.randomUUID()
        val faculty = User().apply { 
            id = userId
            role = Role.FACULTY
        }
        
        val createRequest = CreateRubricsRequest("New Rubrics")
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(faculty))
        
        // When/Then
        val exception = assertThrows<ForbiddenException> {
            rubricsService.createRubrics(createRequest, userId)
        }
        
        assertEquals("Only admin or manager can create rubrics", exception.message)
        verify(userRepository).findById(userId)
        verifyNoInteractions(rubricsRepository)
        verifyNoInteractions(criterionRepository)
    }
}
