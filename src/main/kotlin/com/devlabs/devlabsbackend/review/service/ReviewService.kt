package com.devlabs.devlabsbackend.review.service

import com.devlabs.devlabsbackend.batch.domain.Batch
import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.core.exception.ForbiddenException
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.individualscore.repository.IndividualScoreRepository
import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.project.domain.ProjectStatus
import com.devlabs.devlabsbackend.project.repository.ProjectRepository
import com.devlabs.devlabsbackend.review.domain.DTO.*
import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.review.repository.ReviewRepository
import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import com.devlabs.devlabsbackend.semester.repository.SemesterRepository
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class ReviewService(
    private val projectRepository: ProjectRepository,
    private val reviewRepository: ReviewRepository,
    private val rubricsRepository: RubricsRepository,
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository,
    private val batchRepository: BatchRepository,
    private val userRepository: UserRepository,
    private val individualScoreRepository: IndividualScoreRepository
) {
    @Transactional
    fun createReview(request: CreateReviewRequest, userId: String): Review {
        // Validate user permissions
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        // Only Admin, Manager, and Faculty can create reviews
        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw ForbiddenException("Only admin, manager, or faculty can create reviews")
        }

        // Get rubrics and eagerly load criteria
        val rubrics = rubricsRepository.findById(request.rubricsId).orElseThrow {
            NotFoundException("Rubrics with id ${request.rubricsId} not found")
        }

        // Force loading of criteria to avoid lazy initialization issues
        rubrics.criteria.size

        // Check course permissions if faculty
        if (user.role == Role.FACULTY && !request.courseIds.isNullOrEmpty()) {
            validateFacultyCoursesAccess(user, request.courseIds)
        }
        // Create the review
        val review = Review(
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate,
            rubrics = rubrics,
            createdBy = user
        )
        // Save the review first to get an ID
        val savedReview = reviewRepository.save(review)
        println("DEBUG: Created review ${savedReview.name} with ID ${savedReview.id}")

        // Process requested entities
        if (!request.courseIds.isNullOrEmpty()) {
            println("DEBUG: Adding ${request.courseIds.size} courses directly to review")
            addCoursesToReview(savedReview, request.courseIds, user)
        }

        if (!request.semesterIds.isNullOrEmpty()) {
            println("DEBUG: Adding courses from ${request.semesterIds.size} semesters to review")
            addSemestersToReview(savedReview, request.semesterIds, user)
        }

        if (!request.projectIds.isNullOrEmpty()) {
            println("DEBUG: Adding ${request.projectIds.size} projects to review")
            addProjectsToReview(savedReview, request.projectIds, user)
        }

        if (!request.batchIds.isNullOrEmpty()) {
            println("DEBUG: Adding ${request.batchIds.size} batches to review")
            addBatchesToReview(savedReview, request.batchIds, user)
        }

        val finalReview = reviewRepository.save(savedReview)

        // Ensure all lazy collections are loaded before returning
        finalReview.rubrics?.criteria?.size
        finalReview.courses.size
        finalReview.projects.size
        finalReview.batches.size

        return finalReview
    }

    @Transactional
    fun updateReview(reviewId: UUID, request: UpdateReviewRequest, userId: String): Review {
        // Validate user permissions
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        // Get the review
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        // Check permission to update this review
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can update any review
            }

            Role.FACULTY -> {
                // Faculty can only update reviews they created
                // Handle legacy reviews without createdBy (treat as forbidden for faculty)
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only update reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to update reviews")
            }
        }

        // Update basic properties
        request.name?.let { review.name = it }
        request.startDate?.let { review.startDate = it }
        request.endDate?.let { review.endDate = it }

        // Update rubrics if provided
        if (request.rubricsId != null) {
            val rubrics = rubricsRepository.findById(request.rubricsId).orElseThrow {
                NotFoundException("Rubrics with id ${request.rubricsId} not found")
            }
            review.rubrics = rubrics
        }
        // Process course additions/removals
        if (request.addCourseIds.isNotEmpty()) {
            if (user.role == Role.FACULTY) {
                validateFacultyCoursesAccess(user, request.addCourseIds)
            }
            addCoursesToReview(review, request.addCourseIds, user)
        }

        if (request.removeCourseIds.isNotEmpty()) {
            if (user.role == Role.FACULTY) {
                validateFacultyCoursesAccess(user, request.removeCourseIds)
            }
            removeCoursesFromReview(review, request.removeCourseIds)
        }

        // Process semester additions/removals
        if (request.addSemesterIds.isNotEmpty()) {
            addSemestersToReview(review, request.addSemesterIds, user)
        }

        if (request.removeSemesterIds.isNotEmpty()) {
            removeSemestersFromReview(review, request.removeSemesterIds, user)
        }

        // Process project additions/removals
        if (request.addProjectIds.isNotEmpty()) {
            addProjectsToReview(review, request.addProjectIds, user)
        }

        if (request.removeProjectIds.isNotEmpty()) {
            removeProjectsFromReview(review, request.removeProjectIds, user)
        }

        // Process batch additions/removals
        if (request.addBatchIds.isNotEmpty()) {
            addBatchesToReview(review, request.addBatchIds, user)
        }

        if (request.removeBatchIds.isNotEmpty()) {
            removeBatchesFromReview(review, request.removeBatchIds, user)
        }

        return reviewRepository.save(review)
    }

    @Transactional(readOnly = true)
    fun getReviewById(reviewId: UUID): ReviewResponse {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        // Convert to response within the transaction to avoid lazy loading issues
        return review.toReviewResponse()
    }

    @Transactional(readOnly = true)    
    fun getReviewsForUser(userId: String, page: Int = 0, size: Int = 10, sortBy: String = "startDate", sortOrder: String = "desc"): PaginatedResponse<ReviewResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val sort = createSort(sortBy, sortOrder)
        val pageable = PageRequest.of(page, size, sort)

        val reviewsPage = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can see all reviews
                reviewRepository.findAll(pageable)
            }

            Role.FACULTY -> {
                // Faculty can only see reviews for their courses
                reviewRepository.findByCoursesInstructorsContaining(user, pageable)
            }

            Role.STUDENT -> {
                // Students can see reviews for projects they're part of
                reviewRepository.findByProjectsTeamMembersContaining(user, pageable)
            }

            else -> {
                Page.empty(pageable)
            }
        }

        val reviewResponses = reviewsPage.content.map { it.toReviewResponse() }

        return PaginatedResponse(
            data = reviewResponses,
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = reviewsPage.totalPages,
                total_count = reviewsPage.totalElements.toInt()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getRecentlyCompletedReviews(page: Int = 0, size: Int = 10): PaginatedResponse<ReviewResponse> {
        val today = LocalDate.now()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "endDate"))

        val reviewsPage = reviewRepository.findByEndDateBefore(today, pageable)

        val reviewResponses = reviewsPage.content.map { it.toReviewResponse() }

        return PaginatedResponse(
            data = reviewResponses,
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = reviewsPage.totalPages,
                total_count = reviewsPage.totalElements.toInt()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getLiveReviews(page: Int = 0, size: Int = 10): PaginatedResponse<ReviewResponse> {
        val today = LocalDate.now()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "endDate"))

        val reviewsPage =
            reviewRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today, pageable)

        val reviewResponses = reviewsPage.content.map { it.toReviewResponse() }

        return PaginatedResponse(
            data = reviewResponses,
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = reviewsPage.totalPages,
                total_count = reviewsPage.totalElements.toInt()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getUpcomingReviews(page: Int = 0, size: Int = 10): PaginatedResponse<ReviewResponse> {
        val today = LocalDate.now()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"))

        val reviewsPage = reviewRepository.findByStartDateAfter(today, pageable)

        val reviewResponses = reviewsPage.content.map { it.toReviewResponse() }

        return PaginatedResponse(
            data = reviewResponses,
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = reviewsPage.totalPages,
                total_count = reviewsPage.totalElements.toInt()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getAllReviewsForUser(userId: String): List<ReviewResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val reviews = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can see all reviews
                reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate"))
            }

            Role.FACULTY -> {
                // Faculty can only see reviews for their courses
                reviewRepository.findAllByCourseInstructorsContaining(user)
            }

            Role.STUDENT -> {
                // Students can see reviews for projects they're part of
                reviewRepository.findAllByProjectTeamMembersContaining(user)
            }

            else -> {
                emptyList()
            }
        }

        return reviews.map { it.toReviewResponse() }
    }

    @Transactional(readOnly = true)
    fun getReviewsForProject(projectId: UUID): List<ReviewResponse> {
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        val reviews = reviewRepository.findByProjectsContaining(project)
        return reviews.map { it.toReviewResponse() }
    }


    @Transactional(readOnly = true)
    fun getActiveReviewsForCourse(courseId: UUID): List<ReviewResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }

        val today = LocalDate.now()
        val reviews = reviewRepository.findByCoursesContainingAndEndDateGreaterThanEqual(course, today)
        return reviews.map { it.toReviewResponse() }
    }

    @Transactional
    fun deleteReview(reviewId: UUID, userId: String): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        // Check permission to delete this review
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can delete any review
            }

            Role.FACULTY -> {
                // Faculty can only delete reviews they created
                // Handle legacy reviews without createdBy (treat as forbidden for faculty)
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only delete reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to delete reviews")
            }
        }

        // Remove associations
        val courses = ArrayList(review.courses)
        courses.forEach { course ->
            course.reviews.remove(review)
        }

        reviewRepository.delete(review)
        return true
    }

    /**
     * Get the publication status of a review
     */
    @Transactional(readOnly = true)
    fun getPublicationStatus(reviewId: UUID): ReviewPublicationResponse {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        return ReviewPublicationResponse(
            reviewId = review.id!!,
            reviewName = review.name,
            isPublished = review.isPublished,
            publishDate = review.publishedAt?.toLocalDate()
        )
    }

    /**
     * Publish a review - accessible to ADMIN and MANAGER (any review) and FACULTY (own reviews only)
     */
    @Transactional
    fun publishReview(reviewId: UUID, userId: String): ReviewPublicationResponse {
        // Validate user permissions
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        // Check authorization - Admin and Manager can access anything
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can publish any review
            }

            Role.FACULTY -> {
                // Faculty can only publish reviews they created
                // Handle legacy reviews without createdBy (treat as forbidden for faculty)
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only publish reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to publish reviews")
            }
        }

        if (review.isPublished) {
            throw IllegalArgumentException("Review is already published")
        }

        // Update publication status
        review.isPublished = true
        review.publishedAt = LocalDateTime.now()
        val updatedReview = reviewRepository.save(review)

        return ReviewPublicationResponse(
            reviewId = updatedReview.id!!,
            reviewName = updatedReview.name,
            isPublished = updatedReview.isPublished,
            publishDate = updatedReview.publishedAt?.toLocalDate()
        )
    }

    /**
     * Unpublish a review - accessible to ADMIN and MANAGER (any review) and FACULTY (own reviews only)
     */
    @Transactional
    fun unpublishReview(reviewId: UUID, userId: String): ReviewPublicationResponse {
        // Validate user permissions
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        // Check authorization - Admin and Manager can access anything
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can unpublish any review
            }

            Role.FACULTY -> {
                // Faculty can only unpublish reviews they created
                // Handle legacy reviews without createdBy (treat as forbidden for faculty)
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only unpublish reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to unpublish reviews")
            }
        }

        if (!review.isPublished) {
            throw IllegalArgumentException("Review is already unpublished")
        }

        // Update publication status
        review.isPublished = false
        review.publishedAt = null
        val updatedReview = reviewRepository.save(review)

        return ReviewPublicationResponse(
            reviewId = updatedReview.id!!,
            reviewName = updatedReview.name,
            isPublished = updatedReview.isPublished,
            publishDate = updatedReview.publishedAt?.toLocalDate()
        )
    }

    // Helper methods

    private fun validateFacultyCoursesAccess(faculty: User, courseIds: List<UUID>) {
        val courses = courseRepository.findAllById(courseIds)

        courses.forEach { course ->
            if (!course.instructors.contains(faculty)) {
                throw ForbiddenException("Faculty can only create/update reviews for their own courses")
            }
        }
    }

    @Transactional
    protected fun addCoursesToReview(review: Review, courseIds: List<UUID>, user: User) {
        val courses = courseRepository.findAllByIdWithSemester(courseIds)

        if (user.role == Role.FACULTY) {
            courses.forEach { course ->
                if (!course.instructors.contains(user)) {
                    throw ForbiddenException("Faculty can only add their own courses to reviews")
                }
            }
        }
        courses.forEach { course ->
            // Check if relationship already exists to prevent duplicate key constraint violation
            if (!review.courses.contains(course)) {
                println("DEBUG: Adding course ${course.name} (${course.id}) to review ${review.name} (${review.id})")
                // Only update the owning side (Review) to avoid duplicate inserts
                // Hibernate will automatically sync the inverse side (Course.reviews) when using mappedBy
                review.courses.add(course)
            } else {
                println("DEBUG: Course ${course.name} (${course.id}) already exists in review ${review.name} (${review.id}), skipping")
            }
        }
    }

    @Transactional
    protected fun removeCoursesFromReview(review: Review, courseIds: List<UUID>) {
        val courses = courseRepository.findAllById(courseIds)
        courses.forEach { course ->
            // Only update the owning side (Review) - Hibernate will sync the inverse side automatically
            review.courses.remove(course)
        }
    }

    @Transactional
    protected fun addProjectsToReview(review: Review, projectIds: List<UUID>, user: User) {
        val projects = projectRepository.findAllById(projectIds)

        // Only include Live/Proposed projects
        val validProjects = projects.filter {
            it.status == ProjectStatus.ONGOING || it.status == ProjectStatus.PROPOSED
        }

        if (validProjects.size != projects.size) {
            throw IllegalArgumentException("Some projects are not valid for review (must be Ongoing or Proposed)")
        }

        if (user.role == Role.FACULTY) {
            validProjects.forEach { project ->
                val isInstructorOfProject = project.courses.any { course ->
                    course.instructors.contains(user)
                }
                if (!isInstructorOfProject) {
                    throw ForbiddenException("Faculty can only add projects of their courses to reviews")
                }
            }
        }
        validProjects.forEach { project ->
            // Check if relationship already exists to prevent duplicate key constraint violation
            if (!review.projects.contains(project)) {
                review.projects.add(project)
            }
        }
    }

    @Transactional
    protected fun removeProjectsFromReview(review: Review, projectIds: List<UUID>, user: User) {
        val projects = projectRepository.findAllById(projectIds)

        if (user.role == Role.FACULTY) {
            projects.forEach { project ->
                val isInstructorOfProject = project.courses.any { course ->
                    course.instructors.contains(user)
                }
                if (!isInstructorOfProject) {
                    throw ForbiddenException("Faculty can only remove projects of their courses from reviews")
                }
            }
        }

        projects.forEach { project ->
            review.projects.remove(project)
        }
    }    
    
    @Transactional
    protected fun addBatchesToReview(review: Review, batchIds: List<UUID>, user: User) {
        val batches = batchRepository.findAllById(batchIds)

        if (user.role == Role.FACULTY) {
            batches.forEach { batch ->
                val allCourses = courseRepository.findAll()
                val batchCourses = allCourses.filter { course ->
                    course.batches.contains(batch)
                }
                
                val facultyCourses = batchCourses.filter { course ->
                    course.instructors.contains(user)
                }
                
                if (facultyCourses.isEmpty()) {
                    throw ForbiddenException("Faculty can only assign reviews to batches containing courses they teach. No courses found in batch '${batch.name}' that you instruct.")
                }
                
                facultyCourses.forEach { course ->
                    if (!review.courses.contains(course)) {
                        review.courses.add(course)
                    }
                }
            }
        } else {
            batches.forEach { batch ->
                if (!review.batches.contains(batch)) {
                    review.batches.add(batch)
                }
            }
        }    }    @Transactional
    protected fun removeBatchesFromReview(review: Review, batchIds: List<UUID>, user: User) {
        val batches = batchRepository.findAllById(batchIds)

        if (user.role == Role.FACULTY) {
            batches.forEach { batch ->
                val allCourses = courseRepository.findAll()
                val batchCourses = allCourses.filter { course ->
                    course.batches.contains(batch)
                }
                
                val facultyCourses = batchCourses.filter { course ->
                    course.instructors.contains(user)
                }
                
                if (facultyCourses.isEmpty()) {
                    throw ForbiddenException("Faculty can only remove reviews from batches containing courses they teach.")
                }
                
                facultyCourses.forEach { course ->
                    review.courses.remove(course)
                }
            }
        } else {
            batches.forEach { batch ->
                review.batches.remove(batch)
            }
        }
    }

    @Transactional
    protected fun addSemestersToReview(review: Review, semesterIds: List<UUID>, user: User) {
        val semesters = semesterRepository.findAllByIdWithCourses(semesterIds)

        semesters.forEach { semester ->
            // Get all courses in this semester (now eagerly loaded)
            val semesterCourses = semester.courses

            // Add each course to the review
            semesterCourses.forEach { course ->
                // Check faculty permissions if needed
                if (user.role == Role.FACULTY && !course.instructors.contains(user)) {
                    throw ForbiddenException("Faculty can only add reviews to courses they instruct")
                }                // Check if relationship already exists to prevent duplicate key constraint violation
                if (!review.courses.contains(course)) {
                    println("DEBUG: Adding course ${course.name} (${course.id}) to review ${review.name} (${review.id}) via semester ${semester.name}")
                    // Only update the owning side (Review) to avoid duplicate inserts
                    // Hibernate will automatically sync the inverse side (Course.reviews) when using mappedBy
                    review.courses.add(course)
                } else {
                    println("DEBUG: Course ${course.name} (${course.id}) already exists in review ${review.name} (${review.id}) via semester ${semester.name}, skipping")
                }
            }
        }
    }

    @Transactional
    protected fun removeSemestersFromReview(review: Review, semesterIds: List<UUID>, user: User) {
        val semesters = semesterRepository.findAllByIdWithCourses(semesterIds)

        semesters.forEach { semester ->
            // Get all courses in this semester (now eagerly loaded)
            val semesterCourses = semester.courses            // Remove each course from the review
            semesterCourses.forEach { course ->
                // Only update the owning side (Review) - Hibernate will sync the inverse side automatically
                review.courses.remove(course)
            }
        }
    }

    @Transactional(readOnly = true)
    fun searchReviews(
        userId: String,
        name: String?,
        courseId: UUID?,
        status: String?,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "startDate",
        sortOrder: String = "desc"
    ): PaginatedResponse<ReviewResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val direction = if (sortOrder.uppercase() == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val sort = Sort.by(direction, sortBy)
        val pageable = PageRequest.of(page, size, sort)

        // Get base query results based on user role
        val today = LocalDate.now()

        // First, get reviews based on user role
        var reviews: List<Review> = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                // Admins and managers can see all reviews
                reviewRepository.findAll().toList()
            }

            Role.FACULTY -> {
                // Faculty can only see reviews for their courses
                reviewRepository.findAllByCourseInstructorsContaining(user)
            }

            Role.STUDENT -> {
                // Students can see reviews for projects they're part of
                reviewRepository.findAllByProjectTeamMembersContaining(user)
            }

            else -> {
                emptyList()
            }
        }

        // Then apply search filters
        if (!name.isNullOrBlank()) {
            // Filter by name if provided
            reviews = reviews.filter {
                it.name.contains(name, ignoreCase = true)
            }
        }

        // Then filter by course if provided
        if (courseId != null) {
            val course = courseRepository.findById(courseId).orElseThrow {
                NotFoundException("Course with id $courseId not found")
            }
            reviews = reviews.filter { it.courses.contains(course) }
        }

        // Then filter by status if provided
        if (!status.isNullOrBlank()) {
            reviews = when (status.lowercase()) {
                "live", "ongoing", "current" -> {
                    reviews.filter {
                        it.startDate <= today && it.endDate >= today
                    }
                }

                "completed", "ended", "past" -> {
                    reviews.filter { it.endDate < today }
                }

                "upcoming", "future" -> {
                    reviews.filter { it.startDate > today }
                }

                else -> {
                    throw IllegalArgumentException("Invalid status: $status. Valid values: live, completed, upcoming")
                }
            }
        }

        // Apply pagination manually since we're filtering in memory
        val total = reviews.size
        val totalPages = (total + size - 1) / size

        val paginatedReviews = reviews
            .sortedWith(
                when (sortBy) {
                    "endDate" -> if (direction == Sort.Direction.ASC) compareBy { it.endDate } else compareByDescending { it.endDate }
                    "name" -> if (direction == Sort.Direction.ASC) compareBy { it.name } else compareByDescending { it.name }
                    else -> if (direction == Sort.Direction.ASC) compareBy { it.startDate } else compareByDescending { it.startDate }
                }
            )
            .drop(page * size)
            .take(size)

        return PaginatedResponse(
            data = paginatedReviews.map { it.toReviewResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = total
            )
        )
    }

    /**
     * Checks if a project has any reviews (direct or indirect) that are live or completed
     *
     * @param projectId The ID of the project to check
     * @return ReviewAssignmentResponse containing information about any assigned reviews
     */
    @Transactional(readOnly = true)
    fun checkProjectReviewAssignment(projectId: UUID): ReviewAssignmentResponse {
        println("=== DEBUG: Starting review assignment check for project $projectId ===")

        val today = LocalDate.now()
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        println("DEBUG: Found project: ${project.title}")

        // Initialize project relationships to avoid lazy loading issues
        project.courses.size
        project.team.members.size

        println("DEBUG: Project has ${project.courses.size} courses")
        project.courses.forEach { course ->
            println("DEBUG: - Course: ${course.name} (ID: ${course.id})")
        }        // Get all reviews from the database with all associations eagerly loaded
        val allReviews = reviewRepository.findAllWithAssociations()
        println("DEBUG: Found ${allReviews.size} total reviews in database")

        println("DEBUG: Review IDs: ${allReviews.map { it.id }}")

        allReviews.forEach { review ->
            // Ensure all collections are properly initialized
            println("DEBUG: Review '${review.name}' (ID: ${review.id}) - Initializing collections")
            println("DEBUG: Courses count: ${review.courses.size}")
            println("DEBUG: Projects count: ${review.projects.size}")
            println("DEBUG: Batches count: ${review.batches.size}")

            review.courses.forEach { course ->
                println("DEBUG: - Review course: ${course.name} (ID: ${course.id})")
            }

            review.projects.forEach { proj ->
                println("DEBUG: - Review project: ${proj.title} (ID: ${proj.id})")
            }
        }

        val foundReviews = mutableSetOf<Review>()
        var assignmentType = "NONE"

        // 1. Check for direct project assignment
        val directReviews = allReviews.filter { review ->
            review.projects.any { it.id == project.id }
        }
        println("DEBUG: Found ${directReviews.size} direct project reviews")
        if (directReviews.isNotEmpty()) {
            foundReviews.addAll(directReviews)
            assignmentType = "DIRECT"
        }
        // 2. Check for course-based assignment
        val projectCourses = project.courses
        if (projectCourses.isNotEmpty()) {
            // Find reviews associated with any courses of this project
            val courseReviews = allReviews.filter { review ->
                // Check for direct course-review relationship
                review.courses.any { reviewCourse ->
                    projectCourses.any { projectCourse -> projectCourse.id == reviewCourse.id }
                }
            }
            println("DEBUG: Found ${courseReviews.size} course-based reviews")
            if (courseReviews.isNotEmpty()) {
                foundReviews.addAll(courseReviews)
                if (assignmentType == "NONE") assignmentType = "COURSE"
            }
        }

        // 3. Check for batch-based assignment (through team members)
        val teamMembers = project.team.members
        if (teamMembers.isNotEmpty()) {
            // Get all batches that contain any of the team members
            val memberBatches = mutableSetOf<Batch>()
            teamMembers.forEach { member ->
                val batches = batchRepository.findByStudentsContaining(member)
                memberBatches.addAll(batches)
            }

            if (memberBatches.isNotEmpty()) {
                // Check for reviews directly assigned to these batches
                val batchReviews = allReviews.filter { review ->
                    review.batches.any { batch -> memberBatches.any { it.id == batch.id } }
                }
                if (batchReviews.isNotEmpty()) {
                    foundReviews.addAll(batchReviews)
                    if (assignmentType == "NONE") assignmentType = "BATCH"
                }

                // Check for reviews assigned to courses that include these batches
                val allCourses = courseRepository.findAll()
                val batchCourses = allCourses.filter { course ->
                    course.batches.any { batch -> memberBatches.any { it.id == batch.id } }
                }

                if (batchCourses.isNotEmpty()) {
                    val batchCourseReviews = allReviews.filter { review ->
                        review.courses.any { course -> batchCourses.any { it.id == course.id } }
                    }
                    if (batchCourseReviews.isNotEmpty()) {
                        foundReviews.addAll(batchCourseReviews)
                        if (assignmentType == "NONE") assignmentType = "BATCH_COURSE"
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
                // Get all courses in this semester (now eagerly loaded)
                val semesterCourses = semester.courses

                // Find reviews assigned to courses in this semester
                val semesterCourseReviews = allReviews.filter { review ->
                    review.courses.any { course -> semesterCourses.any { it.id == course.id } }
                }

                if (semesterCourseReviews.isNotEmpty()) {
                    foundReviews.addAll(semesterCourseReviews)
                    if (assignmentType == "NONE") assignmentType = "SEMESTER"
                }

                // Find reviews assigned to batches in this semester
                val semesterBatches = semester.batches
                val semesterBatchReviews = allReviews.filter { review ->
                    review.batches.any { batch -> semesterBatches.any { it.id == batch.id } }
                }

                if (semesterBatchReviews.isNotEmpty()) {
                    foundReviews.addAll(semesterBatchReviews)
                    if (assignmentType == "NONE") assignmentType = "SEMESTER"
                }
            }
        }
        // If no reviews found, return empty response
        if (foundReviews.isEmpty()) {
            return ReviewAssignmentResponse(
                hasReview = false,
                assignmentType = "NONE",
                liveReviews = emptyList(),
                upcomingReviews = emptyList(),
                completedReviews = emptyList()
            )
        }
        // Filter reviews by status (live or completed)
        val liveReviews = foundReviews.filter {
            it.startDate <= today && it.endDate >= today
        }.sortedBy { it.name }.toList()

        val completedReviews = foundReviews.filter {
            it.endDate < today
        }.sortedBy { it.name }.toList()

        val upcomingReviews = foundReviews.filter {
            it.startDate > today
        }.sortedBy { it.name }.toList()

        return ReviewAssignmentResponse(
            hasReview = foundReviews.isNotEmpty(),
            assignmentType = assignmentType,
            liveReviews = liveReviews.map { it.toReviewResponse() },
            upcomingReviews = upcomingReviews.map { it.toReviewResponse() },
            completedReviews = completedReviews.map { it.toReviewResponse() }
        )
    }

    /**
     * Get review results for a specific review, project, and user
     * Access control:
     * - Students: Only see their own scores and only if review is published
     * - Faculty/Admin/Manager: See all scores regardless of publication status
     */
    @Transactional(readOnly = true)
    fun getReviewResults(reviewId: UUID, projectId: UUID, userId: String): ReviewResultsResponse {
        // Get the user making the request
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        // Get the review
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        // Get the project
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        // Verify the project is associated with the review
        if (!isProjectAssociatedWithReview(review, project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }

        // Check access permissions based on user role
        val canViewAllResults = when (user.role) {
            Role.ADMIN, Role.MANAGER -> true
            Role.FACULTY -> {
                // Faculty can view all results for projects in courses they teach
                project.courses.any { course -> course.instructors.contains(user) }
            }

            Role.STUDENT -> {
                // Students can only view results if published and they're part of the project
                if (!review.isPublished) {
                    // Return empty results for unpublished reviews instead of throwing exception
                    return ReviewResultsResponse(
                        id = project.id!!,
                        title = project.title,
                        projectTitle = project.title,
                        reviewName = review.name,
                        isPublished = false,
                        canViewAllResults = false,
                        results = emptyList()
                    )
                }
                if (!project.team.members.contains(user)) {
                    throw ForbiddenException("Students can only view results for their own projects")
                }
                false // Students can only see their own results
            }

            else -> throw ForbiddenException("Invalid user role")
        }

        // Get all individual scores for this review and project
        val allScores = individualScoreRepository.findByReviewAndProject(review, project)

        // Group scores by participant
        val scoresByParticipant = allScores.groupBy { it.participant }

        // Filter participants based on access control
        val filteredParticipants = if (canViewAllResults) {
            scoresByParticipant.keys
        } else {
            // For students, only show their own results
            scoresByParticipant.keys.filter { it.id == user.id }
        }
        // Build results for each participant
        val results = filteredParticipants.map { participant ->
            val participantScores = scoresByParticipant[participant] ?: emptyList()

            // Calculate total scores
            val totalScore = participantScores.sumOf { it.score }
            val maxPossibleScore = participantScores.sumOf { it.criterion.maxScore.toDouble() }
            val percentage = if (maxPossibleScore > 0.0) {
                (totalScore / maxPossibleScore) * 100.0
            } else {
                0.0
            }

            // Build criterion results
            val criterionResults = participantScores.map { score ->
                CriterionResult(
                    criterionId = score.criterion.id!!,
                    criterionName = score.criterion.name,
                    score = score.score,
                    maxScore = score.criterion.maxScore.toDouble(),
                    comment = score.comment
                )
            }

            StudentResult(
                id = participant.id!!,
                name = participant.name,
                studentId = participant.id!!,
                studentName = participant.name,
                individualScore = totalScore,
                totalScore = totalScore,
                maxPossibleScore = maxPossibleScore,
                percentage = percentage,
                scores = criterionResults
            )
        }

        return ReviewResultsResponse(
            id = project.id!!,
            title = project.title,
            projectTitle = project.title,
            reviewName = review.name,
            isPublished = review.isPublished ?: false,
            canViewAllResults = canViewAllResults,
            results = results
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
            val memberBatches = mutableSetOf<Batch>()
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
        for (course in projectCourses) {
            course.semester.id?.let { semesters.add(it) }
        }

        // Get semesters from team member batches
        for (member in teamMembers) {
            val batches = batchRepository.findByStudentsContaining(member)
            for (batch in batches) {
                for (semester in batch.semester) {
                    semester.id?.let { semesters.add(it) }
                }
            }
        }

        if (semesters.isNotEmpty()) {
            // Check if any of these semesters have courses associated with the review
            for (semesterId in semesters) {
                val semester = semesterRepository.findById(semesterId).orElse(null)
                if (semester != null) {
                    val semesterCourses = semester.courses
                    if (review.courses.any { course -> semesterCourses.any { it.id == course.id } }) {
                        return true
                    }
                }
            }
        }

        return false
    }

    // ...existing code...

    fun addFileToReview(
        reviewId: UUID,
        url: String,
    ) {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        // Validate URL format
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Invalid URL format: $url")
        }

        // Add file URL to the review
        review.files.add(url)
        reviewRepository.save(review)
    }

    fun removeFileFromReview(
        reviewId: UUID,
        url: String,
    ) {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        // Validate URL format
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Invalid URL format: $url")
        }
        // Remove file URL from the review
        if (!review.files.remove(url)) {
            throw IllegalArgumentException("File URL $url not found in review ${review.name}")
        }
        reviewRepository.save(review)
    }
}


/**
 * Data class to represent the response for review assignment check
 */
data class ReviewAssignmentResponse(
    val hasReview: Boolean,
    val assignmentType: String,
    val liveReviews: List<ReviewResponse>,
    val upcomingReviews: List<ReviewResponse> = emptyList(),
    val completedReviews: List<ReviewResponse>
)

// Extension function to convert Review to ReviewResponse
fun Review.toReviewResponse(): ReviewResponse {
    return ReviewResponse(
        id = this.id!!,
        name = this.name,
        startDate = this.startDate,
        endDate = this.endDate,
        isPublished = this.isPublished,
        publishedAt = this.publishedAt?.toLocalDate(),
        createdBy = this.createdBy?.let { creator ->
            CreatedByInfo(
                id = creator.id!!,
                name = creator.name,
                email = creator.email,
                role = creator.role.name
            )
        } ?: CreatedByInfo(
            id = "",
            name = "Unknown",
            email = "",
            role = "UNKNOWN"
        ),
        courses = this.courses.map {
            CourseInfo(
                id = it.id!!,
                name = it.name,
                code = it.code,
                semesterInfo = SemesterInfo(
                    id = it.semester.id!!,
                    name = it.semester.name,
                    year = it.semester.year,
                    isActive = it.semester.isActive
                )
            )
        },
        projects = this.projects.map {
            ProjectInfo(
                id = it.id!!,
                title = it.title,
                teamName = it.team.name,
                teamMembers = it.team.members.map { member ->
                    TeamMemberInfo(
                        id = member.id!!,
                        name = member.name
                    )
                }
            )
        },
        sections = emptyList(),
        rubricsInfo = this.rubrics?.let {
            RubricInfo(
                id = it.id!!,
                name = it.name,
                criteria = it.criteria.map { criterion ->
                    CriteriaInfo(
                        id = criterion.id!!,
                        name = criterion.name,
                        description = criterion.description,
                        maxScore = criterion.maxScore,
                        isCommon = criterion.isCommon
                    )
                }            )
        } ?: throw IllegalStateException("Review must have rubrics")
    )
}

private fun createSort(sortBy: String, sortOrder: String): Sort {
    val direction = if (sortOrder.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
    return Sort.by(direction, sortBy)
}