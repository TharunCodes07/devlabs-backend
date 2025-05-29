package com.devlabs.devlabsbackend.review.repository

import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.sql.Timestamp
import java.util.*

@RepositoryRestResource(path = "reviews")
interface ReviewRepository : JpaRepository<Review, UUID> {
    
    // Find reviews by project
    fun findByProject(project: Project): List<Review>
      // Find reviews by reviewer
    fun findByReviewer(reviewer: User): List<Review>
    
    // Find reviews scheduled for a specific date range
    @Query("SELECT r FROM Review r WHERE r.startDate BETWEEN :startDate AND :endDate")
    fun findReviewsInDateRange(@Param("startDate") startDate: Timestamp, @Param("endDate") endDate: Timestamp): List<Review>
    
    // Find latest review for a project
    @Query("SELECT r FROM Review r WHERE r.project = :project ORDER BY r.createdAt DESC")
    fun findLatestByProject(@Param("project") project: Project): List<Review>
}
