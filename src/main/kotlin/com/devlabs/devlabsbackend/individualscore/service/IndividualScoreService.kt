package com.devlabs.devlabsbackend.individualscore.service

import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.individualscore.domain.DTO.*
import com.devlabs.devlabsbackend.individualscore.domain.IndividualScore
import com.devlabs.devlabsbackend.individualscore.repository.IndividualScoreRepository
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class IndividualScoreService(
    private val individualScoreRepository: IndividualScoreRepository,
    private val reviewRepository: ReviewRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val criterionRepository: CriterionRepository,
    private val courseRepository: CourseRepository
) {
    
    @Transactional
    fun submitScores(request: SubmitScoreRequest, submitterId: UUID): List<IndividualScore> {
        val submitter = userRepository.findById(submitterId).orElseThrow {
            NotFoundException("User with id $submitterId not found")
        }
        
        val review = reviewRepository.findById(request.reviewId).orElseThrow {
            NotFoundException("Review with id ${request.reviewId} not found")
        }
        
        val project = projectRepository.findById(request.projectId).orElseThrow {
            NotFoundException("Project with id ${request.projectId} not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if user has permission to submit scores
        if (submitter.role == Role.FACULTY) {
            val isInstructorOfProjectCourse = project.courses.any { course ->
                course.instructors.contains(submitter)
            }
            if (!isInstructorOfProjectCourse) {
                throw ForbiddenException("Faculty can only submit scores for projects in their courses")
            }
        } else if (submitter.role != Role.ADMIN && submitter.role != Role.MANAGER) {
            throw ForbiddenException("Only faculty, admin, or manager can submit scores")
        }
        
        // Get all criteria for this review
        val criteria = review.rubrics?.criteria ?: throw IllegalArgumentException("Review has no rubrics with criteria")
        
        // Map criteria by ID for quick lookup
        val criteriaMap = criteria.associateBy { it.id }
        
        val savedScores = mutableListOf<IndividualScore>()
        
        // Process each participant's scores
        request.scores.forEach { participantScores ->
            val participant = userRepository.findById(participantScores.participantId).orElseThrow {
                NotFoundException("Participant with id ${participantScores.participantId} not found")
            }
            
            if (!project.team.members.contains(participant)) {
                throw IllegalArgumentException("Participant ${participant.name} is not a member of this project team")
            }
            
            // Process each criterion score
            participantScores.criterionScores.forEach { criterionScore ->
                val criterion = criteriaMap[criterionScore.criterionId] ?: throw NotFoundException(
                    "Criterion with id ${criterionScore.criterionId} not found in review's rubrics"
                )
                
                // Validate score is within allowed range
                if (criterionScore.score < 0 || criterionScore.score > criterion.maxScore) {
                    throw IllegalArgumentException(
                        "Score for criterion ${criterion.name} must be between 0 and ${criterion.maxScore}"
                    )
                }
                
                // Find existing score or create new one
                val existingScore = individualScoreRepository.findByParticipantAndCriterionAndReviewAndProject(
                    participant, 
                    criterion,
                    review,
                    project
                )
                
                val score = if (existingScore != null) {
                    // Update existing score
                    existingScore.score = criterionScore.score
                    existingScore.comment = criterionScore.comment
                    existingScore
                } else {
                    // Create new score
                    IndividualScore(
                        participant = participant,
                        criterion = criterion,
                        score = criterionScore.score,
                        comment = criterionScore.comment,
                        review = review,
                        project = project
                    )
                }
                
                savedScores.add(individualScoreRepository.save(score))
            }
        }
        
        return savedScores
    }
    
    /**
     * Submit scores for a specific course. Faculty can only submit scores for courses they teach.
     */
    @Transactional
    fun submitCourseScores(request: SubmitCourseScoreRequest, submitterId: UUID): List<IndividualScore> {
        val submitter = userRepository.findById(submitterId).orElseThrow {
            NotFoundException("User with id $submitterId not found")
        }
        
        val review = reviewRepository.findById(request.reviewId).orElseThrow {
            NotFoundException("Review with id ${request.reviewId} not found")
        }
        
        val project = projectRepository.findById(request.projectId).orElseThrow {
            NotFoundException("Project with id ${request.projectId} not found")
        }
        
        val course = courseRepository.findById(request.courseId).orElseThrow {
            NotFoundException("Course with id ${request.courseId} not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if course is associated with the project
        if (!project.courses.contains(course)) {
            throw IllegalArgumentException("Course is not associated with this project")
        }
        
        // Check if user has permission to submit scores for this course
        if (submitter.role == Role.FACULTY) {
            if (!course.instructors.contains(submitter)) {
                throw ForbiddenException("Faculty can only submit scores for courses they teach")
            }
        } else if (submitter.role != Role.ADMIN && submitter.role != Role.MANAGER) {
            throw ForbiddenException("Only faculty, admin, or manager can submit scores")
        }
        
        // Get all criteria for this review
        val criteria = review.rubrics?.criteria ?: throw IllegalArgumentException("Review has no rubrics with criteria")
        
        // Map criteria by ID for quick lookup
        val criteriaMap = criteria.associateBy { it.id }
        
        val savedScores = mutableListOf<IndividualScore>()
        
        // Process each participant's scores
        request.scores.forEach { participantScores ->
            val participant = userRepository.findById(participantScores.participantId).orElseThrow {
                NotFoundException("Participant with id ${participantScores.participantId} not found")
            }
            
            if (!project.team.members.contains(participant)) {
                throw IllegalArgumentException("Participant ${participant.name} is not a member of this project team")
            }
            
            // Process each criterion score
            participantScores.criterionScores.forEach { criterionScore ->
                val criterion = criteriaMap[criterionScore.criterionId] ?: throw NotFoundException(
                    "Criterion with id ${criterionScore.criterionId} not found in review's rubrics"
                )
                
                // Validate score is within allowed range
                if (criterionScore.score < 0 || criterionScore.score > criterion.maxScore) {
                    throw IllegalArgumentException(
                        "Score for criterion ${criterion.name} must be between 0 and ${criterion.maxScore}"
                    )
                }
                
                // Find existing score or create new one
                val existingScore = individualScoreRepository.findByParticipantAndCriterionAndReviewAndProjectAndCourse(
                    participant, 
                    criterion,
                    review,
                    project,
                    course
                )
                
                val score = if (existingScore != null) {
                    // Update existing score
                    existingScore.score = criterionScore.score
                    existingScore.comment = criterionScore.comment
                    existingScore
                } else {
                    // Create new score
                    IndividualScore(
                        participant = participant,
                        criterion = criterion,
                        score = criterionScore.score,
                        comment = criterionScore.comment,
                        review = review,
                        project = project,
                        course = course
                    )
                }
                
                savedScores.add(individualScoreRepository.save(score))
            }
        }
        
        return savedScores
    }
    
    @Transactional(readOnly = true)
    fun getScoresForParticipant(reviewId: UUID, projectId: UUID, participantId: UUID): ParticipantScoresSummary {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        val participant = userRepository.findById(participantId).orElseThrow {
            NotFoundException("Participant with id $participantId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if participant is member of the project team
        if (!project.team.members.contains(participant)) {
            throw IllegalArgumentException("Participant is not a member of this project team")
        }
        
        // Get all criteria for this review
        val criteria = review.rubrics?.criteria ?: throw IllegalArgumentException("Review has no rubrics with criteria")
        
        // Get scores for this participant in this review and project
        val scores = individualScoreRepository.findByParticipantAndReviewAndProject(
            participant, review, project
        )
        
        // Map scores by criterion ID for quick lookup
        val scoresMap = scores.associateBy { it.criterion.id }
        
        // Build response with scores for each criterion
        val criterionScores = criteria.map { criterion ->
            val score = scoresMap[criterion.id]
            CriterionScoreDetail(
                criterionId = criterion.id!!,
                criterionName = criterion.name,
                maxScore = criterion.maxScore.toDouble(),
                score = score?.score ?: 0.0,
                comment = score?.comment
            )
        }
        
        // Calculate total score and percentage
        val totalScore = criterionScores.sumOf { it.score }
        val maxPossibleScore = criteria.sumOf { it.maxScore.toDouble() }
        val percentage = if (maxPossibleScore > 0) (totalScore / maxPossibleScore) * 100 else 0.0
        
        return ParticipantScoresSummary(
            participantId = participant.id!!,
            participantName = participant.name,
            criterionScores = criterionScores,
            totalScore = totalScore,
            maxPossibleScore = maxPossibleScore,
            percentage = percentage
        )
    }
    
    @Transactional(readOnly = true)
    fun getScoresForProject(reviewId: UUID, projectId: UUID): List<ParticipantScoresSummary> {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Get all team members
        val teamMembers = project.team.members
        
        // Get scores for each team member
        return teamMembers.map { member ->
            getScoresForParticipant(reviewId, projectId, member.id!!)
        }
    }
    
    @Transactional
    fun deleteScoresForParticipant(reviewId: UUID, projectId: UUID, participantId: UUID, submitterId: UUID): Boolean {
        val submitter = userRepository.findById(submitterId).orElseThrow {
            NotFoundException("User with id $submitterId not found")
        }
        
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        val participant = userRepository.findById(participantId).orElseThrow {
            NotFoundException("Participant with id $participantId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if user has permission to delete scores
        if (submitter.role == Role.FACULTY) {
            val isInstructorOfProjectCourse = project.courses.any { course ->
                course.instructors.contains(submitter)
            }
            if (!isInstructorOfProjectCourse) {
                throw ForbiddenException("Faculty can only delete scores for projects in their courses")
            }
        } else if (submitter.role != Role.ADMIN && submitter.role != Role.MANAGER) {
            throw ForbiddenException("Only faculty, admin, or manager can delete scores")
        }
        
        // Delete all scores for this participant in this review and project
        individualScoreRepository.deleteByParticipantAndReviewAndProject(participant, review, project)
        
        return true
    }
    
    @Transactional(readOnly = true)
    fun getIndividualScoreById(scoreId: UUID): IndividualScoreResponse {
        val score = individualScoreRepository.findById(scoreId).orElseThrow {
            NotFoundException("Individual score with id $scoreId not found")
        }
        
        return IndividualScoreResponse(
            id = score.id!!,
            participantId = score.participant.id!!,
            participantName = score.participant.name,
            criterionId = score.criterion.id!!,
            criterionName = score.criterion.name,
            score = score.score,
            comment = score.comment,
            reviewId = score.review.id!!,
            projectId = score.project.id!!
        )
    }
    
    /**
     * Get available evaluations for a faculty member (only courses they teach)
     */
    @Transactional(readOnly = true)
    fun getAvailableEvaluations(request: AvailableEvaluationRequest): AvailableEvaluationResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            NotFoundException("User with id ${request.userId} not found")
        }
          val evaluations = mutableListOf<CourseEvaluationInfo>()
        
        when (user.role) {
            Role.FACULTY -> {                // Get courses where user is an instructor
                val instructorCourses = courseRepository.findCoursesByActiveSemesters().filter { it.instructors.contains(user) }
                
                // Get all active reviews
                val allActiveReviews = reviewRepository.findAll()
                    .filter { it.endDate.isAfter(java.time.LocalDate.now()) }
                
                instructorCourses.forEach { course ->
                    // Find reviews that are associated with this course or have projects related to this course
                    val activeReviews = allActiveReviews.filter { review ->
                        review.courses.contains(course) || 
                        review.projects.any { project -> project.courses.contains(course) }
                    }
                    
                    activeReviews.forEach { review ->
                        // Get projects in this course that are part of the review
                        val courseProjects = if (review.projects.isEmpty()) {
                            // If review has no projects, add a dummy evaluation entry for the course
                            emptyList()
                        } else {
                            review.projects.filter { it.courses.contains(course) }
                        }
                          if (courseProjects.isEmpty() && review.courses.contains(course)) {
                            // Add an evaluation entry for reviews directly linked to courses without projects
                            evaluations.add(
                                CourseEvaluationInfo(
                                    reviewId = review.id!!,
                                    reviewName = review.name,
                                    projectId = UUID.fromString("00000000-0000-0000-0000-000000000000"), // Empty UUID
                                    projectTitle = "No specific project",
                                    courseId = course.id!!,
                                    courseName = course.name,
                                    teamName = "N/A",
                                    startDate = review.startDate,
                                    endDate = review.endDate,
                                    hasExistingEvaluation = false
                                )
                            )
                        } else {
                            // Add evaluation entries for each project
                            courseProjects.forEach { project ->
                                evaluations.add(
                                    CourseEvaluationInfo(
                                        reviewId = review.id!!,
                                        reviewName = review.name,
                                        projectId = project.id!!,
                                        projectTitle = project.title,
                                        courseId = course.id!!,
                                        courseName = course.name,
                                        teamName = project.team.name,
                                        startDate = review.startDate,
                                        endDate = review.endDate,
                                        hasExistingEvaluation = individualScoreRepository.findByReviewAndProjectAndCourse(
                                            review, project, course
                                        ).isNotEmpty()
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can see all available evaluations
                val activeReviews = reviewRepository.findAll()
                    .filter { it.endDate.isAfter(java.time.LocalDate.now()) }
                
                activeReviews.forEach { review ->
                    review.projects.forEach { project ->
                        project.courses.forEach { course ->
                            evaluations.add(
                                CourseEvaluationInfo(
                                    reviewId = review.id!!,
                                    reviewName = review.name,
                                    projectId = project.id!!,
                                    projectTitle = project.title,
                                    courseId = course.id!!,
                                    courseName = course.name,
                                    teamName = project.team.name,
                                    startDate = review.startDate,
                                    endDate = review.endDate,
                                    hasExistingEvaluation = individualScoreRepository.findByReviewAndProjectAndCourse(
                                        review, project, course
                                    ).isNotEmpty()
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                throw ForbiddenException("Only faculty, admin, or manager can access evaluations")
            }
        }
        
        return AvailableEvaluationResponse(
            evaluations = evaluations,
            totalCount = evaluations.size
        )
    }
    
    /**
     * Get course-specific scores for a participant
     */
    @Transactional(readOnly = true)
    fun getCourseScoresForParticipant(
        reviewId: UUID, 
        projectId: UUID, 
        participantId: UUID, 
        courseId: UUID
    ): ParticipantScoresSummary {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        val participant = userRepository.findById(participantId).orElseThrow {
            NotFoundException("Participant with id $participantId not found")
        }
        
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if course is associated with the project
        if (!project.courses.contains(course)) {
            throw IllegalArgumentException("Course is not associated with this project")
        }
        
        // Check if participant is member of the project team
        if (!project.team.members.contains(participant)) {
            throw IllegalArgumentException("Participant is not a member of this project team")
        }
        
        // Get all criteria for this review
        val criteria = review.rubrics?.criteria ?: throw IllegalArgumentException("Review has no rubrics with criteria")
        
        // Get scores for this participant in this review, project, and course
        val scores = individualScoreRepository.findByParticipantAndReviewAndProjectAndCourse(
            participant, review, project, course
        )
        
        // Map scores by criterion ID for quick lookup
        val scoresMap = scores.associateBy { it.criterion.id }
        
        // Build response with scores for each criterion
        val criterionScores = criteria.map { criterion ->
            val score = scoresMap[criterion.id]
            CriterionScoreDetail(
                criterionId = criterion.id!!,
                criterionName = criterion.name,
                maxScore = criterion.maxScore.toDouble(),
                score = score?.score ?: 0.0,
                comment = score?.comment
            )
        }
        
        // Calculate total score and percentage
        val totalScore = criterionScores.sumOf { it.score }
        val maxPossibleScore = criteria.sumOf { it.maxScore.toDouble() }
        val percentage = if (maxPossibleScore > 0) (totalScore / maxPossibleScore) * 100 else 0.0
        
        return ParticipantScoresSummary(
            participantId = participant.id!!,
            participantName = participant.name,
            criterionScores = criterionScores,
            totalScore = totalScore,
            maxPossibleScore = maxPossibleScore,
            percentage = percentage
        )
    }
    
    /**
     * Get course-specific scores for a project
     */
    @Transactional(readOnly = true)
    fun getCourseScoresForProject(reviewId: UUID, projectId: UUID, courseId: UUID): List<ParticipantScoresSummary> {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if course is associated with the project
        if (!project.courses.contains(course)) {
            throw IllegalArgumentException("Course is not associated with this project")
        }
        
        // Get all team members
        val teamMembers = project.team.members
        
        // Get scores for each team member for this course
        return teamMembers.map { member ->
            getCourseScoresForParticipant(reviewId, projectId, member.id!!, courseId)
        }
    }
    
    /**
     * Get evaluation summary by course for a project
     */
    @Transactional(readOnly = true)
    fun getProjectEvaluationSummary(reviewId: UUID, projectId: UUID): ProjectEvaluationSummary {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        val courseEvaluations = project.courses.map { course ->
            val hasScores = individualScoreRepository.findByReviewAndProjectAndCourse(
                review, project, course
            ).isNotEmpty()
            
            CourseEvaluationSummary(
                courseId = course.id!!,
                courseName = course.name,
                instructors = course.instructors.map { it.name },
                hasEvaluation = hasScores,
                evaluationCount = if (hasScores) {
                    individualScoreRepository.findDistinctParticipantsByReviewAndProjectAndCourse(
                        review, project, course
                    ).size
                } else 0
            )
        }
        
        return ProjectEvaluationSummary(
            reviewId = review.id!!,
            reviewName = review.name,
            projectId = project.id!!,
            projectTitle = project.title,
            teamName = project.team.name,
            courseEvaluations = courseEvaluations
        )
    }
    
    /**
     * Delete course-specific scores for a participant
     */
    @Transactional
    fun deleteCourseScoresForParticipant(
        reviewId: UUID, 
        projectId: UUID, 
        participantId: UUID, 
        courseId: UUID, 
        submitterId: UUID
    ): Boolean {
        val submitter = userRepository.findById(submitterId).orElseThrow {
            NotFoundException("User with id $submitterId not found")
        }
        
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        val participant = userRepository.findById(participantId).orElseThrow {
            NotFoundException("Participant with id $participantId not found")
        }
        
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Check if project is part of the review
        if (!review.projects.contains(project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if course is associated with the project
        if (!project.courses.contains(course)) {
            throw IllegalArgumentException("Course is not associated with this project")
        }
        
        // Check if user has permission to delete scores for this course
        if (submitter.role == Role.FACULTY) {
            if (!course.instructors.contains(submitter)) {
                throw ForbiddenException("Faculty can only delete scores for courses they teach")
            }
        } else if (submitter.role != Role.ADMIN && submitter.role != Role.MANAGER) {
            throw ForbiddenException("Only faculty, admin, or manager can delete scores")
        }
        
        // Delete all scores for this participant in this review, project, and course
        individualScoreRepository.deleteByParticipantAndReviewAndProjectAndCourse(
            participant, review, project, course
        )
        
        return true
    }
}
