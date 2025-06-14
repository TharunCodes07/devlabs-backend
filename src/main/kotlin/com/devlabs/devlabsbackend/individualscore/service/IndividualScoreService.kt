package com.devlabs.devlabsbackend.individualscore.service

import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.criterion.repository.CriterionRepository
import com.devlabs.devlabsbackend.individualscore.domain.DTO.*
import com.devlabs.devlabsbackend.individualscore.domain.IndividualScore
import com.devlabs.devlabsbackend.individualscore.repository.IndividualScoreRepository
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.rubrics.domain.Rubrics
import com.devlabs.devlabsbackend.semester.repository.SemesterRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
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
    private val courseRepository: CourseRepository,
    private val batchRepository: BatchRepository,
    private val semesterRepository: SemesterRepository
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
    fun getScoresForParticipant(reviewId: UUID, projectId: UUID, participantId: UUID, requesterId: UUID? = null): ParticipantScoresSummary {
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
        if (!isProjectAssociatedWithReview(review, project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if participant is member of the project team
        if (!project.team.members.contains(participant)) {
            throw IllegalArgumentException("Participant is not a member of this project team")
        }
        
        // Check access rights if requester ID is provided
        if (requesterId != null) {
            checkScoreAccessRights(requesterId, review, project, participantId)
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
     */    @Transactional(readOnly = true)
    fun getProjectEvaluationSummary(reviewId: UUID, projectId: UUID): ProjectEvaluationSummary {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
          val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Check if project is part of the review
        if (!isProjectAssociatedWithReview(review, project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Eagerly fetch the courses to avoid lazy loading issues
        val courses = project.courses.toList()
          val courseEvaluations = courses.map { course ->
            val hasScores = individualScoreRepository.findByReviewAndProjectAndCourse(
                review, project, course
            ).isNotEmpty()
            
            CourseEvaluationSummary(
                courseId = course.id!!,
                courseName = course.name,
                instructors = course.instructors.map { instructor ->
                    InstructorInfo(
                        id = instructor.id!!,
                        name = instructor.name
                    )
                },
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
        if (!isProjectAssociatedWithReview(review, project)) {
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
    
    /**
     * Check if user has access to view scores based on role and review publication status
     * This method is used by controllers to enforce access control
     */
    fun checkScoreAccessRights(userId: UUID, review: Review, project: Project, participantId: UUID? = null) {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
        // Admins and Managers always have access
        if (user.role == Role.ADMIN || user.role == Role.MANAGER) {
            return
        }
        
        // Faculty access check - must be teaching the course
        if (user.role == Role.FACULTY) {
            val isInstructorOfProjectCourse = project.courses.any { course ->
                course.instructors.contains(user)
            }
            
            if (!isInstructorOfProjectCourse) {
                throw ForbiddenException("Faculty can only view scores for projects in their courses")
            }
            return
        }
        
        // Student access check - must be the participant and review must be published
        if (user.role == Role.STUDENT) {
            // Check if review is published
            if (!review.isPublished) {
                throw ForbiddenException("Students can only view scores for published reviews")
            }
            
            // Check if student is viewing their own scores
            if (participantId != null && participantId != user.id) {
                throw ForbiddenException("Students can only view their own scores")
            }
            
            // Check if student is a member of the project team
            if (!project.team.members.contains(user)) {
                throw ForbiddenException("Students can only view scores for projects they are part of")
            }
            
            return
        }
        
        // If we get here, access is denied
        throw ForbiddenException("Unauthorized access to scores")
    }
    
    /**
     * Get a student's own scores for a specific review and project
     */
    @Transactional(readOnly = true)
    fun getStudentScores(reviewId: UUID, projectId: UUID, studentId: UUID): ParticipantScoresSummary {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }
        
        // Verify access rights
        checkScoreAccessRights(studentId, review, project, studentId)
        
        // Get the student
        val student = userRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with id $studentId not found")
        }
        
        // Get scores
        val scores = individualScoreRepository.findByParticipantAndReviewAndProject(student, review, project)
        
        // Convert to response format
        return createParticipantScoresSummary(student, scores, review.rubrics!!)
    }
    
    private fun createParticipantScoresSummary(
        participant: User,
        scores: List<IndividualScore>,
        rubrics: Rubrics
    ): ParticipantScoresSummary {
        // Map scores by criterion ID for quick lookup
        val scoresMap = scores.associateBy { it.criterion.id }
        
        // Build response with scores for each criterion
        val criterionScores = rubrics.criteria.map { criterion ->
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
        val maxPossibleScore = rubrics.criteria.sumOf { it.maxScore.toDouble() }
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
     * Comprehensive check to determine if a project is associated with a review
     * through any valid relationship (direct, course, batch, or semester)
     */
    private fun isProjectAssociatedWithReview(review: Review, project: Project): Boolean {
        // Initialize project relationships to avoid lazy loading issues
        project.courses.size
        project.team.members.size
        
        // 1. Check for direct project assignment
        if (review.projects.any { it.id == project.id }) {
            return true
        }
        
        // 2. Check for course-based assignment
        val projectCourses = project.courses
        if (projectCourses.isNotEmpty()) {
            // Check for direct course-review relationship
            if (review.courses.any { reviewCourse -> 
                projectCourses.any { projectCourse -> projectCourse.id == reviewCourse.id } 
            }) {
                return true
            }
        }
        
        // 3. Check for batch-based assignment (through team members)
        val teamMembers = project.team.members
        if (teamMembers.isNotEmpty()) {
            // Get all batches that contain any of the team members
            val memberBatches = mutableSetOf<com.devlabs.devlabsbackend.batch.domain.Batch>()
            teamMembers.forEach { member ->
                val batches = batchRepository.findByStudentsContaining(member)
                memberBatches.addAll(batches)
            }
            
            if (memberBatches.isNotEmpty()) {
                // Check for reviews directly assigned to these batches
                if (review.batches.any { batch -> memberBatches.any { it.id == batch.id } }) {
                    return true
                }
                
                // Check for reviews assigned to courses that include these batches
                val allCourses = courseRepository.findAll()
                val batchCourses = allCourses.filter { course ->
                    course.batches.any { batch -> memberBatches.any { it.id == batch.id } }
                }
                
                if (batchCourses.isNotEmpty()) {
                    if (review.courses.any { course -> batchCourses.any { it.id == course.id } }) {
                        return true
                    }
                }
            }
        }
        
        // 4. Check for semester-based assignment
        val semesters = mutableSetOf<UUID>()
        
        // Get semesters from project courses
        projectCourses.forEach { course ->
            course.semester.id?.let { semesters.add(it) }
        }
        
        // Get semesters from team member batches
        teamMembers.forEach { member ->
            val batches = batchRepository.findByStudentsContaining(member)
            batches.forEach { batch ->
                batch.semester.forEach { semester ->
                    semester.id?.let { semesters.add(it) }
                }
            }
        }
        
        if (semesters.isNotEmpty()) {
            val semesterEntities = semesterRepository.findAllByIdWithCourses(semesters.toList())
            semesterEntities.forEach { semester ->
                // Get all courses in this semester
                val semesterCourses = semester.courses
                
                // Find reviews assigned to courses in this semester
                if (review.courses.any { course -> semesterCourses.any { it.id == course.id } }) {
                    return true
                }
                
                // Find reviews assigned to batches in this semester
                val semesterBatches = semester.batches
                if (review.batches.any { batch -> semesterBatches.any { it.id == batch.id } }) {
                    return true
                }
            }
        }
          return false
    }

    /**
     * Get comprehensive course evaluation data including team members, criteria, and existing scores
     */
    @Transactional(readOnly = true)
    fun getCourseEvaluationData(reviewId: UUID, projectId: UUID, courseId: UUID, userId: UUID): CourseEvaluationData {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
        
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
        if (!isProjectAssociatedWithReview(review, project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }
        
        // Check if course is associated with the project
        if (!project.courses.contains(course)) {
            throw IllegalArgumentException("Course is not associated with this project")
        }
        
        // Check user access permissions
        checkCourseEvaluationAccess(user, review, project, course)
        
        // Get team members
        val teamMembers = project.team.members.map { member ->
            TeamMemberInfo(
                id = member.id!!,
                name = member.name,
                email = member.email,
                role = member.role.name
            )
        }
        
        // Get criteria from review rubrics
        val criteria = review.rubrics?.criteria?.map { criterion ->
            CriterionInfo(
                id = criterion.id!!,
                name = criterion.name,
                description = criterion.description,
                maxScore = criterion.maxScore.toDouble(),
                courseSpecific = true // All criteria in course evaluations are course-specific
            )
        } ?: emptyList()
        
        // Get existing scores for this review, project, and course
        val existingScores = project.team.members.mapNotNull { member ->
            val scores = individualScoreRepository.findByParticipantAndReviewAndProjectAndCourse(
                member, review, project, course
            )
            
            if (scores.isNotEmpty()) {
                ParticipantScoreData(
                    participantId = member.id!!,
                    criterionScores = scores.map { score ->
                        CriterionScoreData(
                            criterionId = score.criterion.id!!,
                            score = score.score,
                            comment = score.comment
                        )
                    }
                )
            } else {
                null
            }
        }
        
        return CourseEvaluationData(
            courseId = course.id!!,
            courseName = course.name,
            projectId = project.id!!,
            reviewId = review.id!!,
            teamMembers = teamMembers,
            criteria = criteria,
            existingScores = existingScores,
            isPublished = review.isPublished ?: false
        )
    }
    
    /**
     * Check if user has access to course evaluation data
     */
    private fun checkCourseEvaluationAccess(user: User, review: Review, project: Project, course: com.devlabs.devlabsbackend.course.domain.Course) {
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers have access to all evaluations
                return
            }
            Role.FACULTY -> {
                // Faculty can only access courses they teach
                if (!course.instructors.contains(user)) {
                    throw ForbiddenException("Faculty can only access evaluations for courses they teach")
                }
            }
            Role.STUDENT -> {
                // Students can only access their own evaluations in published reviews
                if (review.isPublished != true) {
                    throw ForbiddenException("Students can only access published reviews")
                }
                
                // Check if student is a member of the project team
                if (!project.team.members.contains(user)) {
                    throw ForbiddenException("Students can only access their own project evaluations")
                }
            }
        }
    }
}
