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
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        if (user.role != Role.ADMIN && user.role != Role.MANAGER && user.role != Role.FACULTY) {
            throw ForbiddenException("Only admin, manager, or faculty can create reviews")
        }

        val rubrics = rubricsRepository.findById(request.rubricsId).orElseThrow {
            NotFoundException("Rubrics with id ${request.rubricsId} not found")
        }

        rubrics.criteria.size

        if (user.role == Role.FACULTY && !request.courseIds.isNullOrEmpty()) {
            validateFacultyCoursesAccess(user, request.courseIds)
        }
        val review = Review(
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate,
            rubrics = rubrics,
            createdBy = user
        )
        val savedReview = reviewRepository.save(review)
        println("DEBUG: Created review ${savedReview.name} with ID ${savedReview.id}")

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

        finalReview.rubrics?.criteria?.size
        finalReview.courses.size
        finalReview.projects.size
        finalReview.batches.size

        return finalReview
    }

    @Transactional
    fun updateReview(reviewId: UUID, request: UpdateReviewRequest, userId: String): Review {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
            }

            Role.FACULTY -> {
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only update reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to update reviews")
            }
        }

        request.name?.let { review.name = it }
        request.startDate?.let { review.startDate = it }
        request.endDate?.let { review.endDate = it }

        if (request.rubricsId != null) {
            val rubrics = rubricsRepository.findById(request.rubricsId).orElseThrow {
                NotFoundException("Rubrics with id ${request.rubricsId} not found")
            }
            review.rubrics = rubrics
        }
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

        if (request.addSemesterIds.isNotEmpty()) {
            addSemestersToReview(review, request.addSemesterIds, user)
        }

        if (request.removeSemesterIds.isNotEmpty()) {
            removeSemestersFromReview(review, request.removeSemesterIds, user)
        }

        if (request.addProjectIds.isNotEmpty()) {
            addProjectsToReview(review, request.addProjectIds, user)
        }

        if (request.removeProjectIds.isNotEmpty()) {
            removeProjectsFromReview(review, request.removeProjectIds, user)
        }

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
                reviewRepository.findAll(pageable)
            }

            Role.FACULTY -> {
                reviewRepository.findByCoursesInstructorsContaining(user, pageable)
            }

            Role.STUDENT -> {
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


    @Transactional
    fun deleteReview(reviewId: UUID, userId: String): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
            }

            Role.FACULTY -> {
                if (review.createdBy?.id != user.id) {
                    throw ForbiddenException("You can only delete reviews that you created")
                }
            }

            else -> {
                throw ForbiddenException("You don't have permission to delete reviews")
            }
        }

        val courses = ArrayList(review.courses)
        courses.forEach { course ->
            course.reviews.remove(review)
        }

        reviewRepository.delete(review)
        return true
    }

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


    @Transactional
    fun publishReview(reviewId: UUID, userId: String): ReviewPublicationResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }
        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
            }

            Role.FACULTY -> {
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

    @Transactional
    fun unpublishReview(reviewId: UUID, userId: String): ReviewPublicationResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
            }

            Role.FACULTY -> {
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


     fun validateFacultyCoursesAccess(faculty: User, courseIds: List<UUID>) {
        val courses = courseRepository.findAllById(courseIds)

        courses.forEach { course ->
            if (!course.instructors.contains(faculty)) {
                throw ForbiddenException("Faculty can only create/update reviews for their own courses")
            }
        }
    }

    @Transactional
     fun addCoursesToReview(review: Review, courseIds: List<UUID>, user: User) {
        val courses = courseRepository.findAllByIdWithSemester(courseIds)

        if (user.role == Role.FACULTY) {
            courses.forEach { course ->
                if (!course.instructors.contains(user)) {
                    throw ForbiddenException("Faculty can only add their own courses to reviews")
                }
            }
        }
        courses.forEach { course ->
            if (!review.courses.contains(course)) {
                println("DEBUG: Adding course ${course.name} (${course.id}) to review ${review.name} (${review.id})")
                review.courses.add(course)
            } else {
                println("DEBUG: Course ${course.name} (${course.id}) already exists in review ${review.name} (${review.id}), skipping")
            }
        }
    }

    @Transactional
     fun removeCoursesFromReview(review: Review, courseIds: List<UUID>) {
        val courses = courseRepository.findAllById(courseIds)
        courses.forEach { course ->
            review.courses.remove(course)
        }
    }

    @Transactional
     fun addProjectsToReview(review: Review, projectIds: List<UUID>, user: User) {
        val projects = projectRepository.findAllById(projectIds)

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
            if (!review.projects.contains(project)) {
                review.projects.add(project)
            }
        }
    }

    @Transactional
     fun removeProjectsFromReview(review: Review, projectIds: List<UUID>, user: User) {
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
     fun addBatchesToReview(review: Review, batchIds: List<UUID>, user: User) {
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
     fun removeBatchesFromReview(review: Review, batchIds: List<UUID>, user: User) {
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
     fun addSemestersToReview(review: Review, semesterIds: List<UUID>, user: User) {
        val semesters = semesterRepository.findAllByIdWithCourses(semesterIds)

        semesters.forEach { semester ->
            val semesterCourses = semester.courses

            semesterCourses.forEach { course ->
                if (user.role == Role.FACULTY && !course.instructors.contains(user)) {
                    throw ForbiddenException("Faculty can only add reviews to courses they instruct")
                }
                if (!review.courses.contains(course)) {
                    println("DEBUG: Adding course ${course.name} (${course.id}) to review ${review.name} (${review.id}) via semester ${semester.name}")
                    review.courses.add(course)
                } else {
                    println("DEBUG: Course ${course.name} (${course.id}) already exists in review ${review.name} (${review.id}) via semester ${semester.name}, skipping")
                }
            }
        }
    }

    @Transactional
     fun removeSemestersFromReview(review: Review, semesterIds: List<UUID>, user: User) {
        val semesters = semesterRepository.findAllByIdWithCourses(semesterIds)

        semesters.forEach { semester ->
            val semesterCourses = semester.courses
            semesterCourses.forEach { course ->
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

        val today = LocalDate.now()

        var reviews: List<Review> = when (user.role) {
            Role.ADMIN, Role.MANAGER -> {
                reviewRepository.findAll().toList()
            }

            Role.FACULTY -> {
                reviewRepository.findAllByCourseInstructorsContaining(user)
            }

            Role.STUDENT -> {
                reviewRepository.findAllByProjectTeamMembersContaining(user)
            }

            else -> {
                emptyList()
            }
        }

        if (!name.isNullOrBlank()) {
            reviews = reviews.filter {
                it.name.contains(name, ignoreCase = true)
            }
        }

        if (courseId != null) {
            val course = courseRepository.findById(courseId).orElseThrow {
                NotFoundException("Course with id $courseId not found")
            }
            reviews = reviews.filter { it.courses.contains(course) }
        }

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

    @Transactional(readOnly = true)
    fun checkProjectReviewAssignment(projectId: UUID): ReviewAssignmentResponse {
        println("=== DEBUG: Starting review assignment check for project $projectId ===")

        val today = LocalDate.now()
        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        println("DEBUG: Found project: ${project.title}")

        project.courses.size
        project.team.members.size

        println("DEBUG: Project has ${project.courses.size} courses")
        project.courses.forEach { course ->
            println("DEBUG: - Course: ${course.name} (ID: ${course.id})")
        }
        val allReviews = reviewRepository.findAllWithAssociations()
        println("DEBUG: Found ${allReviews.size} total reviews in database")

        println("DEBUG: Review IDs: ${allReviews.map { it.id }}")

        allReviews.forEach { review ->
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

        val directReviews = allReviews.filter { review ->
            review.projects.any { it.id == project.id }
        }
        println("DEBUG: Found ${directReviews.size} direct project reviews")
        if (directReviews.isNotEmpty()) {
            foundReviews.addAll(directReviews)
            assignmentType = "DIRECT"
        }
        val projectCourses = project.courses
        if (projectCourses.isNotEmpty()) {
            val courseReviews = allReviews.filter { review ->
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

        val teamMembers = project.team.members
        if (teamMembers.isNotEmpty()) {
            val memberBatches = mutableSetOf<Batch>()
            teamMembers.forEach { member ->
                val batches = batchRepository.findByStudentsContaining(member)
                memberBatches.addAll(batches)
            }

            if (memberBatches.isNotEmpty()) {
                val batchReviews = allReviews.filter { review ->
                    review.batches.any { batch -> memberBatches.any { it.id == batch.id } }
                }
                if (batchReviews.isNotEmpty()) {
                    foundReviews.addAll(batchReviews)
                    if (assignmentType == "NONE") assignmentType = "BATCH"
                }

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

        val semesters = mutableSetOf<UUID>()

        projectCourses.forEach { course ->
            course.semester.id?.let { semesters.add(it) }
        }

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
                val semesterCourses = semester.courses

                val semesterCourseReviews = allReviews.filter { review ->
                    review.courses.any { course -> semesterCourses.any { it.id == course.id } }
                }

                if (semesterCourseReviews.isNotEmpty()) {
                    foundReviews.addAll(semesterCourseReviews)
                    if (assignmentType == "NONE") assignmentType = "SEMESTER"
                }

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
        if (foundReviews.isEmpty()) {
            return ReviewAssignmentResponse(
                hasReview = false,
                assignmentType = "NONE",
                liveReviews = emptyList(),
                upcomingReviews = emptyList(),
                completedReviews = emptyList()
            )
        }
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

    @Transactional(readOnly = true)
    fun getReviewResults(reviewId: UUID, projectId: UUID, userId: String): ReviewResultsResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        val project = projectRepository.findById(projectId).orElseThrow {
            NotFoundException("Project with id $projectId not found")
        }

        if (!isProjectAssociatedWithReview(review, project)) {
            throw IllegalArgumentException("Project is not part of this review")
        }

        val canViewAllResults = when (user.role) {
            Role.ADMIN, Role.MANAGER -> true
            Role.FACULTY -> {
                project.courses.any { course -> course.instructors.contains(user) }
            }

            Role.STUDENT -> {
                if (!review.isPublished) {
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
                false
            }

            else -> throw ForbiddenException("Invalid user role")
        }

        val allScores = individualScoreRepository.findByReviewAndProject(review, project)

        val scoresByParticipant = allScores.groupBy { it.participant }

        val filteredParticipants = if (canViewAllResults) {
            scoresByParticipant.keys
        } else {
            scoresByParticipant.keys.filter { it.id == user.id }
        }
        val results = filteredParticipants.map { participant ->
            val participantScores = scoresByParticipant[participant] ?: emptyList()

            val totalScore = participantScores.sumOf { it.score }
            val maxPossibleScore = participantScores.sumOf { it.criterion.maxScore.toDouble() }
            val percentage = if (maxPossibleScore > 0.0) {
                (totalScore / maxPossibleScore) * 100.0
            } else {
                0.0
            }

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

    private fun isProjectAssociatedWithReview(review: Review, project: Project): Boolean {
        project.courses.size
        project.team.members.size

        if (review.projects.any { it.id == project.id }) {
            return true
        }

        val projectCourses = project.courses
        if (projectCourses.isNotEmpty()) {
            if (review.courses.any { reviewCourse ->
                    projectCourses.any { projectCourse -> projectCourse.id == reviewCourse.id }
                }) {
                return true
            }
        }

        val teamMembers = project.team.members
        if (teamMembers.isNotEmpty()) {
            val memberBatches = mutableSetOf<Batch>()
            teamMembers.forEach { member ->
                val batches = batchRepository.findByStudentsContaining(member)
                memberBatches.addAll(batches)
            }

            if (memberBatches.isNotEmpty()) {
                if (review.batches.any { batch -> memberBatches.any { it.id == batch.id } }) {
                    return true
                }

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

        val semesters = mutableSetOf<UUID>()
        for (course in projectCourses) {
            course.semester.id?.let { semesters.add(it) }
        }

        for (member in teamMembers) {
            val batches = batchRepository.findByStudentsContaining(member)
            for (batch in batches) {
                for (semester in batch.semester) {
                    semester.id?.let { semesters.add(it) }
                }
            }
        }

        if (semesters.isNotEmpty()) {
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

    fun addFileToReview(
        reviewId: UUID,
        url: String,
    ) {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            NotFoundException("Review with id $reviewId not found")
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Invalid URL format: $url")
        }

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
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Invalid URL format: $url")
        }
        if (!review.files.remove(url)) {
            throw IllegalArgumentException("File URL $url not found in review ${review.name}")
        }
        reviewRepository.save(review)
    }
}

data class ReviewAssignmentResponse(
    val hasReview: Boolean,
    val assignmentType: String,
    val liveReviews: List<ReviewResponse>,
    val upcomingReviews: List<ReviewResponse> = emptyList(),
    val completedReviews: List<ReviewResponse>
)

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