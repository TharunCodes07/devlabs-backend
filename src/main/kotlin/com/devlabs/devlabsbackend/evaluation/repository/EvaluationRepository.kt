package com.devlabs.devlabsbackend.evaluation.repository

import com.devlabs.devlabsbackend.evaluation.domain.Evaluation
import com.devlabs.devlabsbackend.evaluation.domain.EvaluationStatus
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EvaluationRepository : JpaRepository<Evaluation, UUID> {
    fun findByReviewAndProject(review: Review, project: Project): List<Evaluation>
    
    fun findByReviewAndProjectAndEvaluator(review: Review, project: Project, evaluator: User): Optional<Evaluation>
    
    fun findByReview(review: Review): List<Evaluation>
    
    fun findByReviewAndStatus(review: Review, status: EvaluationStatus): List<Evaluation>
    
    fun findByProjectAndStatus(project: Project, status: EvaluationStatus): List<Evaluation>
    
    @Query("SELECT e FROM Evaluation e WHERE e.review.id = :reviewId AND e.project.id = :projectId")
    fun findByReviewIdAndProjectId(@Param("reviewId") reviewId: UUID, @Param("projectId") projectId: UUID): List<Evaluation>
}
