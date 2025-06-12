package com.devlabs.devlabsbackend.rubrics.controller

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.criterion.domain.dto.CriterionResponse
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateCriterionRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.CreateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.domain.dto.RubricsResponse
import com.devlabs.devlabsbackend.rubrics.domain.dto.UpdateRubricsRequest
import com.devlabs.devlabsbackend.rubrics.service.RubricsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(RubricsController::class)
class RubricsControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var rubricsService: RubricsService
    
    private lateinit var objectMapper: ObjectMapper
    
    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }
    
    @Test
    fun `getAllRubrics returns 200 OK with paged rubrics`() {
        // Given
        val rubrics1 = RubricsResponse(
            id = UUID.randomUUID(),
            name = "Rubrics 1",
            criteria = listOf(
                CriterionResponse(
                    id = UUID.randomUUID(),
                    name = "Criterion 1",
                    description = "Description 1",
                    maxScore = 10.0f,
                    isCommon = true
                )
            )
        )
        
        val rubrics2 = RubricsResponse(
            id = UUID.randomUUID(),
            name = "Rubrics 2",
            criteria = emptyList()
        )
        
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        val rubricsPage = PageImpl(listOf(rubrics1, rubrics2), pageable, 2)
        
        `when`(rubricsService.getAllRubrics(eq(pageable))).thenReturn(rubricsPage)
        
        // When/Then
        mockMvc.perform(get("/api/rubrics")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "ASC"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].name").value("Rubrics 1"))
            .andExpect(jsonPath("$.data[1].name").value("Rubrics 2"))
            .andExpect(jsonPath("$.pagination.totalElements").value(2))
            
        verify(rubricsService).getAllRubrics(eq(pageable))
    }
    
    @Test
    fun `getRubricsById returns 200 OK with rubrics when found`() {
        // Given
        val rubricsId = UUID.randomUUID()
        val rubrics = RubricsResponse(
            id = rubricsId,
            name = "Test Rubrics",
            criteria = listOf(
                CriterionResponse(
                    id = UUID.randomUUID(),
                    name = "Criterion 1",
                    description = "Description 1",
                    maxScore = 10.0f,
                    isCommon = true
                )
            )
        )
        
        `when`(rubricsService.getRubricsById(rubricsId)).thenReturn(rubrics)
        
        // When/Then
        mockMvc.perform(get("/api/rubrics/$rubricsId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(rubricsId.toString()))
            .andExpect(jsonPath("$.name").value("Test Rubrics"))
            .andExpect(jsonPath("$.criteria.length()").value(1))
            .andExpect(jsonPath("$.criteria[0].name").value("Criterion 1"))
            
        verify(rubricsService).getRubricsById(rubricsId)
    }
    
    @Test
    fun `getRubricsById returns 404 Not Found when rubrics not found`() {
        // Given
        val rubricsId = UUID.randomUUID()
        
        `when`(rubricsService.getRubricsById(rubricsId))
            .thenThrow(NotFoundException("Rubrics with id $rubricsId not found"))
        
        // When/Then
        mockMvc.perform(get("/api/rubrics/$rubricsId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Rubrics with id $rubricsId not found"))
            
        verify(rubricsService).getRubricsById(rubricsId)
    }
    
    @Test
    fun `createRubrics returns 201 Created with created rubrics`() {
        // Given
        val userId = UUID.randomUUID()
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
        
        val createdRubrics = RubricsResponse(
            id = UUID.randomUUID(),
            name = "New Rubrics",
            criteria = listOf(
                CriterionResponse(
                    id = UUID.randomUUID(),
                    name = "Criterion 1",
                    description = "Description 1",
                    maxScore = 10.0f,
                    isCommon = true
                )
            )
        )
        
        `when`(rubricsService.createRubrics(eq(createRequest), eq(userId)))
            .thenReturn(createdRubrics)
        
        // When/Then
        mockMvc.perform(post("/api/rubrics")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Rubrics"))
            .andExpect(jsonPath("$.criteria.length()").value(1))
            .andExpect(jsonPath("$.criteria[0].name").value("Criterion 1"))
            
        verify(rubricsService).createRubrics(eq(createRequest), eq(userId))
    }
    
    @Test
    fun `createRubrics returns 403 Forbidden when user lacks permission`() {
        // Given
        val userId = UUID.randomUUID()
        val createRequest = CreateRubricsRequest(
            name = "New Rubrics"
        )
        
        `when`(rubricsService.createRubrics(eq(createRequest), eq(userId)))
            .thenThrow(ForbiddenException("Only admin or manager can create rubrics"))
        
        // When/Then
        mockMvc.perform(post("/api/rubrics")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("Only admin or manager can create rubrics"))
            
        verify(rubricsService).createRubrics(eq(createRequest), eq(userId))
    }
}
