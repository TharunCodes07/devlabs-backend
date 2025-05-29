package com.devlabs.devlabsbackend.rubric.service

import com.devlabs.devlabsbackend.rubric.domain.*
import com.devlabs.devlabsbackend.rubric.dto.*
import com.devlabs.devlabsbackend.rubric.repository.*
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.user.repository.UserRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class RubricService(
    private val rubricTemplateRepository: RubricTemplateRepository,
    private val rubricTemplateItemRepository: RubricTemplateItemRepository,
    private val rubricRepository: RubricRepository,
    private val rubricItemRepository: RubricItemRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) {

    // Template management
    fun createTemplate(templateData: CreateTemplateRequest, creatorId: UUID): RubricTemplate {
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }
        
        // Validate creator is faculty/admin
        if (creator.role !in listOf(Role.FACULTY, Role.ADMIN)) {
            throw IllegalArgumentException("Only faculty and admin can create rubric templates")
        }
        
        val template = RubricTemplate(
            name = templateData.name,
            description = templateData.description,
            isPublic = templateData.isPublic,
            createdBy = creator
        )
        
        val savedTemplate = rubricTemplateRepository.save(template)
        
        // Create template items
        templateData.items.forEachIndexed { index, itemData ->
            val item = RubricTemplateItem(
                criteriaName = itemData.criteriaName,
                description = itemData.description,
                type = itemData.type,
                weight = itemData.weight,
                minScore = itemData.minScore,
                maxScore = itemData.maxScore,
                isRequired = itemData.isRequired,
                orderIndex = index,
                template = savedTemplate
            )
            rubricTemplateItemRepository.save(item)
        }
        
        return savedTemplate
    }

    fun updateTemplate(templateId: UUID, updateData: UpdateTemplateRequest, userId: UUID): RubricTemplate {
        val template = getTemplateById(templateId)
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        // Validate user is template creator
        if (template.createdBy.id != user.id) {
            throw IllegalArgumentException("Only template creator can update template")
        }
        
        // Update template fields
        updateData.name?.let { template.name = it }
        updateData.description?.let { template.description = it }
        updateData.isPublic?.let { template.isPublic = it }
        template.updatedAt = Timestamp.from(Instant.now())
        
        return rubricTemplateRepository.save(template)
    }

    fun getAvailableTemplates(userId: UUID): List<RubricTemplate> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        return rubricTemplateRepository.findAvailableTemplates(user)
    }

    fun getTemplatesByCreator(creatorId: UUID): List<RubricTemplate> {
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }
        return rubricTemplateRepository.findByCreatedBy(creator)
    }

    // Rubric management for reviews
    fun createRubricFromTemplate(reviewId: UUID, templateId: UUID, creatorId: UUID): Rubric {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val template = getTemplateById(templateId)
        
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }
        
        // Validate creator is review reviewer
        if (review.reviewer.id != creator.id) {
            throw IllegalArgumentException("Only review reviewer can create rubric")
        }
        
        // Check if rubric already exists for this review
        val existingRubric = rubricRepository.findByReview(review)
        if (existingRubric != null) {
            throw IllegalArgumentException("Rubric already exists for this review")
        }
        
        val rubric = Rubric(
            review = review,
            template = template
        )
        
        val savedRubric = rubricRepository.save(rubric)
        
        // Create rubric items from template
        val templateItems = rubricTemplateItemRepository.findByTemplateOrderByOrderIndex(template)
        templateItems.forEach { templateItem ->
            val rubricItem = RubricItem(
                criteriaName = templateItem.criteriaName,
                description = templateItem.description,
                type = templateItem.type,
                weight = templateItem.weight,
                minScore = templateItem.minScore,
                maxScore = templateItem.maxScore,
                orderIndex = templateItem.orderIndex,
                rubric = savedRubric
            )
            rubricItemRepository.save(rubricItem)
        }
        
        return savedRubric
    }

    fun createCustomRubric(reviewId: UUID, rubricData: CreateCustomRubricRequest, creatorId: UUID): Rubric {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val creator = userRepository.findById(creatorId).orElseThrow {
            NotFoundException("User with id $creatorId not found")
        }
        
        // Validate creator is review reviewer
        if (review.reviewer.id != creator.id) {
            throw IllegalArgumentException("Only review reviewer can create rubric")
        }
        
        // Check if rubric already exists for this review
        val existingRubric = rubricRepository.findByReview(review)
        if (existingRubric != null) {
            throw IllegalArgumentException("Rubric already exists for this review")
        }
        
        val rubric = Rubric(
            review = review,
            template = null // Custom rubric, no template
        )
        
        val savedRubric = rubricRepository.save(rubric)
        
        // Create rubric items
        rubricData.items.forEachIndexed { index, itemData ->
            val rubricItem = RubricItem(
                criteriaName = itemData.criteriaName,
                description = itemData.description,
                type = itemData.type,
                weight = itemData.weight,
                minScore = itemData.minScore,
                maxScore = itemData.maxScore,
                orderIndex = index,
                rubric = savedRubric
            )
            rubricItemRepository.save(rubricItem)
        }
        
        return savedRubric
    }

    fun scoreRubricItem(itemId: UUID, score: Double, feedback: String?, reviewerId: UUID): RubricItem {
        val item = rubricItemRepository.findById(itemId).orElseThrow {
            NotFoundException("Rubric item with id $itemId not found")
        }
        
        val reviewer = userRepository.findById(reviewerId).orElseThrow {
            NotFoundException("User with id $reviewerId not found")
        }
        
        // Validate reviewer is the assigned reviewer
        if (item.rubric.review.reviewer.id != reviewer.id) {
            throw IllegalArgumentException("Only assigned reviewer can score rubric items")
        }
        
        // Validate score is within bounds
        if (item.minScore != null && score < item.minScore!!) {
            throw IllegalArgumentException("Score cannot be less than minimum score ${item.minScore}")
        }
        if (item.maxScore != null && score > item.maxScore!!) {
            throw IllegalArgumentException("Score cannot be greater than maximum score ${item.maxScore}")
        }
        
        item.score = score
        item.feedback = feedback
        
        val savedItem = rubricItemRepository.save(item)
        
        // Update review total score
        updateReviewScores(item.rubric)
        
        return savedItem
    }

    fun getRubricByReview(reviewId: UUID): Rubric? {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        return rubricRepository.findByReview(review)
    }    private fun updateReviewScores(rubric: Rubric) {
        val totalScore = rubric.getTotalScore()
        
        val review = rubric.review
        review.totalScore = totalScore
        review.maxPossibleScore = rubric.template?.getTotalPossibleScore() 
            ?: rubric.items.mapNotNull { it.maxScore }.sum()
        review.percentageScore = review.calculatePercentageScore()
          reviewRepository.save(review)
    }

    private fun getTemplateById(templateId: UUID): RubricTemplate {
        return rubricTemplateRepository.findById(templateId).orElseThrow {
            NotFoundException("Rubric template with id $templateId not found")
        }
    }
}
