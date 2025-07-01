package com.devlabs.devlabsbackend.individualscore.repository

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.individualscore.domain.IndividualScore
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.course.domain.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@RepositoryRestResource(path = "individualScore")
interface IndividualScoreRepository : JpaRepository<IndividualScore, UUID> {
    
    fun findByParticipantAndCriterionAndReviewAndProject(
        participant: User,
        criterion: Criterion,
        review: Review,
        project: Project
    ): IndividualScore?
    
    fun findByParticipantAndReviewAndProject(
        participant: User,
        review: Review,
        project: Project
    ): List<IndividualScore>
    
    fun findByReviewAndProject(
        review: Review,
        project: Project
    ): List<IndividualScore>
    
    fun findByReviewAndProjectAndCourse(
        review: Review,
        project: Project,
        course: Course
    ): List<IndividualScore>
    
    fun findByParticipantAndReviewAndProjectAndCourse(
        participant: User,
        review: Review,
        project: Project,
        course: Course
    ): List<IndividualScore>    fun findByParticipantAndCriterionAndReviewAndProjectAndCourse(
        participant: User,
        criterion: Criterion,
        review: Review,
        project: Project,
        course: Course
    ): IndividualScore?
    
    @Query("SELECT s FROM IndividualScore s WHERE s.participant = :student AND :course MEMBER OF s.project.courses")
    fun findByParticipantAndProjectInCourse(
        @Param("student") student: User,
        @Param("course") course: Course
    ): List<IndividualScore>
    
    @Query("SELECT COALESCE(AVG(s.score / s.criterion.maxScore * 100), 0.0) FROM IndividualScore s WHERE s.participant = :student AND :course MEMBER OF s.project.courses")
    fun getAverageScorePercentageForStudentAndCourse(
        @Param("student") student: User,
        @Param("course") course: Course
    ): Double
    
    @Query("SELECT COUNT(DISTINCT s.review) FROM IndividualScore s WHERE s.participant = :student AND :course MEMBER OF s.project.courses")
    fun countDistinctReviewsForStudentAndCourse(
        @Param("student") student: User,
        @Param("course") course: Course
    ): Int
    
    @Query("SELECT DISTINCT is.participant FROM IndividualScore is WHERE is.review = :review AND is.project = :project")
    fun findDistinctParticipantsByReviewAndProject(
        @Param("review") review: Review,
        @Param("project") project: Project
    ): List<User>
    
    @Query("SELECT DISTINCT is.participant FROM IndividualScore is WHERE is.review = :review AND is.project = :project AND is.course = :course")
    fun findDistinctParticipantsByReviewAndProjectAndCourse(
        @Param("review") review: Review,
        @Param("project") project: Project,
        @Param("course") course: Course
    ): List<User>
    
    @Transactional
    fun deleteByParticipantAndReviewAndProject(
        participant: User,
        review: Review,
        project: Project
    )
    
    @Transactional
    fun deleteByReviewAndProject(
        review: Review,
        project: Project
    )
    
    @Transactional
    fun deleteByParticipantAndReviewAndProjectAndCourse(
        participant: User,
        review: Review,
        project: Project,
        course: Course
    )
    
    @Query("SELECT DISTINCT s.review FROM IndividualScore s WHERE s.participant = :participant AND s.course = :course ORDER BY s.review.startDate")
    fun findDistinctReviewsByParticipantAndCourse(
        @Param("participant") participant: User,
        @Param("course") course: Course
    ): List<Review>
    
    @Query("SELECT s FROM IndividualScore s WHERE s.participant = :participant AND s.review = :review AND s.course = :course")
    fun findByParticipantAndReviewAndCourse(
        @Param("participant") participant: User,
        @Param("review") review: Review,
        @Param("course") course: Course
    ): List<IndividualScore>
}