package com.devlabs.devlabsbackend.rubric.repository

import com.devlabs.devlabsbackend.rubric.domain.RubricTemplate
import com.devlabs.devlabsbackend.rubric.domain.RubricTemplateItem
import com.devlabs.devlabsbackend.rubric.domain.Rubric
import com.devlabs.devlabsbackend.rubric.domain.RubricItem
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.review.domain.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(path = "rubric-templates")
interface RubricTemplateRepository : JpaRepository<RubricTemplate, UUID> {
    
    // Find templates by creator
    fun findByCreatedBy(createdBy: User): List<RubricTemplate>
    
    // Find public templates
    fun findByIsPublicTrue(): List<RubricTemplate>
    
    // Find templates by creator or public templates
    @Query("SELECT rt FROM RubricTemplate rt WHERE rt.createdBy = :user OR rt.isPublic = true")
    fun findAvailableTemplates(@Param("user") user: User): List<RubricTemplate>
    
    // Find templates by name containing (case insensitive)
    fun findByNameContainingIgnoreCase(name: String): List<RubricTemplate>
    
    // Find templates created by user that are either private or public
    @Query("SELECT rt FROM RubricTemplate rt WHERE rt.createdBy = :user AND rt.isPublic = :isPublic")
    fun findByCreatedByAndIsPublic(@Param("user") user: User, @Param("isPublic") isPublic: Boolean): List<RubricTemplate>
}

@RepositoryRestResource(path = "rubric-template-items")
interface RubricTemplateItemRepository : JpaRepository<RubricTemplateItem, UUID> {
    
    // Find items by template
    fun findByTemplate(template: RubricTemplate): List<RubricTemplateItem>
    
    // Find items by template ordered by orderIndex
    fun findByTemplateOrderByOrderIndex(template: RubricTemplate): List<RubricTemplateItem>
    
    // Find required items by template
    @Query("SELECT rti FROM RubricTemplateItem rti WHERE rti.template = :template AND rti.isRequired = true")
    fun findRequiredItemsByTemplate(@Param("template") template: RubricTemplate): List<RubricTemplateItem>
}

@RepositoryRestResource(path = "rubrics")
interface RubricRepository : JpaRepository<Rubric, UUID> {
    
    // Find rubric by review
    fun findByReview(review: Review): Rubric?
    
    // Find rubrics by template
    fun findByTemplate(template: RubricTemplate): List<Rubric>
    
    // Find rubrics without template (custom rubrics)
    fun findByTemplateIsNull(): List<Rubric>
}

@RepositoryRestResource(path = "rubric-items")
interface RubricItemRepository : JpaRepository<RubricItem, UUID> {
    
    // Find items by rubric
    fun findByRubric(rubric: Rubric): List<RubricItem>
    
    // Find items by rubric ordered by orderIndex
    fun findByRubricOrderByOrderIndex(rubric: Rubric): List<RubricItem>
    
    // Find items with scores by rubric
    @Query("SELECT ri FROM RubricItem ri WHERE ri.rubric = :rubric AND ri.score IS NOT NULL")
    fun findScoredItemsByRubric(@Param("rubric") rubric: Rubric): List<RubricItem>
}
