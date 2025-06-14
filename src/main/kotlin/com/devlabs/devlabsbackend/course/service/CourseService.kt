package com.devlabs.devlabsbackend.course.service

import com.devlabs.devlabsbackend.batch.domain.dto.BatchResponse
import com.devlabs.devlabsbackend.batch.repository.BatchRepository
import com.devlabs.devlabsbackend.batch.service.toBatchResponse
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import com.devlabs.devlabsbackend.core.pagination.PaginatedResponse
import com.devlabs.devlabsbackend.core.pagination.PaginationInfo
import com.devlabs.devlabsbackend.course.domain.Course
import com.devlabs.devlabsbackend.course.domain.DTO.CoursePerformanceChartResponse
import com.devlabs.devlabsbackend.course.domain.DTO.CourseResponse
import com.devlabs.devlabsbackend.course.domain.DTO.StudentCourseWithScoresResponse
import com.devlabs.devlabsbackend.course.repository.CourseRepository
import com.devlabs.devlabsbackend.individualscore.repository.IndividualScoreRepository
import com.devlabs.devlabsbackend.user.domain.DTO.UserResponse
import com.devlabs.devlabsbackend.user.domain.Role
import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.user.repository.UserRepository
import com.devlabs.devlabsbackend.user.service.toUserResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val batchRepository: BatchRepository,
    private val individualScoreRepository: IndividualScoreRepository
) {
    @Transactional
    fun assignStudents(courseId: UUID, studentId:List<UUID>){
            val course = courseRepository.findById(courseId).orElseThrow {
                NotFoundException("Could not find course with id $courseId")
            }
        // Force initialization of students collection to avoid LazyInitializationException
        course.students.size
        val users = userRepository.findAllById(studentId)
        course.students.addAll(users)
        courseRepository.save(course)
    }

    @Transactional
    fun removeStudents(courseId: UUID, studentId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        // Force initialization of students collection to avoid LazyInitializationException
        course.students.size
        val users = userRepository.findAllById(studentId)
        course.students.removeAll(users)
        courseRepository.save(course)
    }
    
    @Transactional
    fun assignInstructors(courseId: UUID, instructorId:List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        // Force initialization of instructors collection to avoid LazyInitializationException
        course.instructors.size
        val users = userRepository.findAllById(instructorId)
        course.instructors.addAll(users)
        courseRepository.save(course)
    }

    @Transactional
    fun removeInstructors(courseId: UUID, instructorId:List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow{
            NotFoundException("Could not find course with id $courseId")
        }
        // Force initialization of instructors collection to avoid LazyInitializationException
        course.instructors.size
        val users = userRepository.findAllById(instructorId)
        course.instructors.removeAll(users)
        courseRepository.save(course)
    }

    @Transactional
    fun addBatchesToCourse(courseId: UUID, batchId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Could not find course with id $courseId")
        }
        // Force initialization of batches collection to avoid LazyInitializationException
        course.batches.size
        val batches = batchRepository.findAllById(batchId)
        course.batches.addAll(batches)
        courseRepository.save(course)
    }

    @Transactional
    fun removeBatchesFromCourse(courseId: UUID, batchId: List<UUID>){
        val course = courseRepository.findById(courseId).orElseThrow{
            NotFoundException("Could not find course with id $courseId")
        }
        // Force initialization of batches collection to avoid LazyInitializationException
        course.batches.size
        val batches = batchRepository.findAllById(batchId)
        course.batches.removeAll(batches)
        courseRepository.save(course)
    }

    @Transactional(readOnly = true)
    fun getCourseById(courseId: UUID): CourseResponse {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        return course.toCourseResponse()
    }

    @Transactional(readOnly = true)
    fun getCourseBatches(
        courseId: UUID,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<BatchResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of batches collection to avoid LazyInitializationException
        course.batches.size
        
        // Apply sorting manually since we can't use repository methods for this collection
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        
        // Sort the batches collection manually
        val sortedBatches = when (sortBy.lowercase()) {
            "name" -> if (direction == Sort.Direction.ASC) 
                        course.batches.sortedBy { it.name } 
                      else 
                        course.batches.sortedByDescending { it.name }
            "graduationyear" -> if (direction == Sort.Direction.ASC) 
                                  course.batches.sortedBy { it.graduationYear } 
                                else 
                                  course.batches.sortedByDescending { it.graduationYear }
            "section" -> if (direction == Sort.Direction.ASC) 
                           course.batches.sortedBy { it.section } 
                         else 
                           course.batches.sortedByDescending { it.section }
            else -> course.batches.sortedBy { it.name } // Default sort by name
        }
        
        // Apply pagination
        val totalElements = sortedBatches.size
        val totalPages = (totalElements + size - 1) / size // Ceiling division
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        
        val pagedBatches = if (startIndex < totalElements) {
            sortedBatches.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PaginatedResponse(
            data = pagedBatches.map { it.toBatchResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = totalElements
            )
        )
    }

    @Transactional(readOnly = true)
    fun getCourseStudents(
        courseId: UUID,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of students collection to avoid LazyInitializationException
        course.students.size
        
        // Filter only STUDENT role users
        val studentUsers = course.students.filter { it.role == Role.STUDENT }
        
        // Apply sorting manually since we can't use repository methods for this collection
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        
        // Sort the students collection manually
        val sortedStudents = when (sortBy.lowercase()) {
            "name" -> if (direction == Sort.Direction.ASC) 
                        studentUsers.sortedBy { it.name } 
                      else 
                        studentUsers.sortedByDescending { it.name }
            "email" -> if (direction == Sort.Direction.ASC) 
                         studentUsers.sortedBy { it.email } 
                       else 
                         studentUsers.sortedByDescending { it.email }
            "createdat" -> if (direction == Sort.Direction.ASC) 
                             studentUsers.sortedBy { it.createdAt } 
                           else 
                             studentUsers.sortedByDescending { it.createdAt }
            else -> studentUsers.sortedBy { it.name } // Default sort by name
        }
        
        // Apply pagination
        val totalElements = sortedStudents.size
        val totalPages = (totalElements + size - 1) / size // Ceiling division
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        
        val pagedStudents = if (startIndex < totalElements) {
            sortedStudents.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PaginatedResponse(
            data = pagedStudents.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = totalElements
            )
        )
    }

    @Transactional(readOnly = true)
    fun searchCourseBatches(
        courseId: UUID,
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<BatchResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of batches collection to avoid LazyInitializationException
        course.batches.size
        
        // Filter batches by name or section containing the query
        val filteredBatches = course.batches.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.section.contains(query, ignoreCase = true) ||
            it.graduationYear.toString().contains(query, ignoreCase = true)
        }
        
        // Apply sorting and pagination manually
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        
        // Sort the filtered batches collection manually
        val sortedBatches = when (sortBy.lowercase()) {
            "name" -> if (direction == Sort.Direction.ASC) 
                        filteredBatches.sortedBy { it.name } 
                      else 
                        filteredBatches.sortedByDescending { it.name }
            "graduationyear" -> if (direction == Sort.Direction.ASC) 
                                  filteredBatches.sortedBy { it.graduationYear } 
                                else 
                                  filteredBatches.sortedByDescending { it.graduationYear }
            "section" -> if (direction == Sort.Direction.ASC) 
                           filteredBatches.sortedBy { it.section } 
                         else 
                           filteredBatches.sortedByDescending { it.section }
            else -> filteredBatches.sortedBy { it.name } // Default sort by name
        }
        
        // Apply pagination
        val totalElements = sortedBatches.size
        val totalPages = (totalElements + size - 1) / size // Ceiling division
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        
        val pagedBatches = if (startIndex < totalElements) {
            sortedBatches.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PaginatedResponse(
            data = pagedBatches.map { it.toBatchResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = totalElements
            )
        )
    }

    @Transactional(readOnly = true)
    fun searchCourseStudents(
        courseId: UUID,
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of students collection to avoid LazyInitializationException
        course.students.size
        
        // Filter students by name or email containing the query
        val filteredStudents = course.students.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.email.contains(query, ignoreCase = true) ||
            it.profileId?.contains(query, ignoreCase = true) == true
        }
        
        // Apply sorting and pagination manually
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        
        // Sort the filtered students collection manually
        val sortedStudents = when (sortBy.lowercase()) {
            "name" -> if (direction == Sort.Direction.ASC) 
                        filteredStudents.sortedBy { it.name } 
                      else 
                        filteredStudents.sortedByDescending { it.name }
            "email" -> if (direction == Sort.Direction.ASC) 
                         filteredStudents.sortedBy { it.email } 
                       else 
                         filteredStudents.sortedByDescending { it.email }
            "createdat" -> if (direction == Sort.Direction.ASC) 
                             filteredStudents.sortedBy { it.createdAt } 
                           else 
                             filteredStudents.sortedByDescending { it.createdAt }
            else -> filteredStudents.sortedBy { it.name } // Default sort by name
        }
        
        // Apply pagination
        val totalElements = sortedStudents.size
        val totalPages = (totalElements + size - 1) / size // Ceiling division
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        
        val pagedStudents = if (startIndex < totalElements) {
            sortedStudents.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PaginatedResponse(
            data = pagedStudents.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = totalElements
            )
        )
    }

    // Non-paginated version to be used by the instructor endpoint
    @Transactional(readOnly = true)
    fun getCourseInstructors(
        courseId: UUID
    ): List<UserResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of instructors collection to avoid LazyInitializationException
        course.instructors.size
        
        // Return all instructors without pagination
        return course.instructors.map { it.toUserResponse() }
    }

    @Transactional(readOnly = true)
    fun searchCourseInstructors(
        courseId: UUID,
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<UserResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of instructors collection to avoid LazyInitializationException
        course.instructors.size
        
        // Filter instructors by name or email containing the query
        val filteredInstructors = course.instructors.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.email.contains(query, ignoreCase = true) ||
            it.profileId?.contains(query, ignoreCase = true) == true
        }
        
        // Apply sorting and pagination manually
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        
        // Sort the filtered instructors collection manually
        val sortedInstructors = when (sortBy.lowercase()) {
            "name" -> if (direction == Sort.Direction.ASC) 
                        filteredInstructors.sortedBy { it.name } 
                      else 
                        filteredInstructors.sortedByDescending { it.name }
            "email" -> if (direction == Sort.Direction.ASC) 
                         filteredInstructors.sortedBy { it.email } 
                       else 
                         filteredInstructors.sortedByDescending { it.email }
            "createdat" -> if (direction == Sort.Direction.ASC) 
                             filteredInstructors.sortedBy { it.createdAt } 
                           else 
                             filteredInstructors.sortedByDescending { it.createdAt }
            else -> filteredInstructors.sortedBy { it.name } // Default sort by name
        }
        
        // Apply pagination
        val totalElements = sortedInstructors.size
        val totalPages = (totalElements + size - 1) / size // Ceiling division
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        
        val pagedInstructors = if (startIndex < totalElements) {
            sortedInstructors.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PaginatedResponse(
            data = pagedInstructors.map { it.toUserResponse() },
            pagination = PaginationInfo(
                current_page = page,
                per_page = size,
                total_pages = totalPages,
                total_count = totalElements
            )
        )
    }    @Transactional(readOnly = true)
    fun getUnassignedStudents(courseId: UUID): List<UserResponse> {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Force initialization of students collection to avoid LazyInitializationException
        course.students.size
        
        // Get all users with STUDENT role who are not in the course
        val allStudentUsers = userRepository.findByRole(Role.STUDENT)
        val unassignedStudents = allStudentUsers.filter { student -> 
            !course.students.map { it.id }.contains(student.id)
        }
        
        return unassignedStudents.map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun getCoursesForCurrentUser(
        currentUser: User,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<CourseResponse> {
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable = PageRequest.of(page, size, sort)
        
        val coursePage = when (currentUser.role) {
            Role.MANAGER -> {
                // Managers can see all courses from active semesters
                courseRepository.findCoursesByActiveSemesters(pageable)
            }
            Role.FACULTY -> {
                // Faculty can only see courses where they are instructors in active semesters
                courseRepository.findCoursesByActiveSemestersAndInstructor(currentUser, pageable)
            }
            else -> {
                throw IllegalArgumentException("Access denied. Only MANAGER and FACULTY roles can access this endpoint.")
            }
        }
        
        return PaginatedResponse(
            data = coursePage.content.map { it.toCourseResponse() },
            pagination = PaginationInfo(
                current_page = coursePage.number,
                per_page = coursePage.size,
                total_pages = coursePage.totalPages,
                total_count = coursePage.totalElements.toInt()
            )
        )
    }
    
    @Transactional(readOnly = true)
    fun searchCoursesForCurrentUser(
        currentUser: User,
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<CourseResponse> {
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable = PageRequest.of(page, size, sort)
        
        val coursePage = when (currentUser.role) {
            Role.MANAGER -> {
                // Managers can search all courses from active semesters
                courseRepository.searchCoursesByActiveSemesters(query, pageable)
            }
            Role.FACULTY -> {
                // Faculty can only search courses where they are instructors in active semesters
                courseRepository.searchCoursesByActiveSemestersAndInstructor(currentUser, query, pageable)
            }
            else -> {
                throw IllegalArgumentException("Access denied. Only MANAGER and FACULTY roles can access this endpoint.")
            }
        }
        
        return PaginatedResponse(
            data = coursePage.content.map { it.toCourseResponse() },
            pagination = PaginationInfo(
                current_page = coursePage.number,
                per_page = coursePage.size,
                total_pages = coursePage.totalPages,
                total_count = coursePage.totalElements.toInt()
            )
        )
    }
    
    @Transactional(readOnly = true)
    fun getActiveCoursesForCurrentUser(
        currentUser: User,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): PaginatedResponse<CourseResponse> {
        val direction = if (sortOrder.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortBy)
        val pageable = PageRequest.of(page, size, sort)
          val coursePage = when (currentUser.role) {
            Role.STUDENT -> {
                // Students get courses they're assigned to in active semesters
                // Try direct assignment first, then batch assignment
                val directCoursePage = courseRepository.findCoursesByActiveSemestersAndStudent(currentUser, pageable)
                if (directCoursePage.isEmpty) {
                    courseRepository.findCoursesByActiveSemestersAndStudentThroughBatch(currentUser, pageable)
                } else {
                    directCoursePage
                }
            }
            Role.FACULTY -> {
                // Faculty get courses they're assigned to in active semesters
                courseRepository.findCoursesByActiveSemestersAndInstructor(currentUser, pageable)
            }
            Role.ADMIN, Role.MANAGER -> {
                // Admin/Manager get all courses in active semesters
                courseRepository.findCoursesByActiveSemesters(pageable)
            }
        }
        
        return PaginatedResponse(
            data = coursePage.content.map { it.toCourseResponse() },
            pagination = PaginationInfo(
                current_page = coursePage.number,
                per_page = coursePage.size,
                total_pages = coursePage.totalPages,
                total_count = coursePage.totalElements.toInt()
            )
        )
    }
      @Transactional
    fun getAllActiveCourses(): List<CourseResponse> {
        return courseRepository.findCoursesByActiveSemesters().map { it.toCourseResponse() }
    }
    
    @Transactional(readOnly = true)    fun getActiveCoursesForUser(userId: UUID): List<CourseResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with id $userId not found")
        }
          return when (user.role) {
            Role.STUDENT -> {
                // Students get courses they're assigned to in active semesters
                // Try direct assignment first, then batch assignment
                val directCourses = courseRepository.findCoursesByActiveSemestersAndStudent(user)
                if (directCourses.isEmpty()) {
                    courseRepository.findCoursesByActiveSemestersAndStudentThroughBatch(user).map { it.toCourseResponse() }
                } else {
                    directCourses.map { it.toCourseResponse() }
                }
            }
            Role.FACULTY -> {
                // Faculty get courses they're assigned to in active semesters
                courseRepository.findCoursesByActiveSemestersAndInstructor(user).map { it.toCourseResponse() }
            }
            Role.ADMIN, Role.MANAGER -> {
                // Admin/Manager get all courses in active semesters
                courseRepository.findCoursesByActiveSemesters().map { it.toCourseResponse() }
            }
        }
    }
      @Transactional(readOnly = true)
    fun getStudentActiveCoursesWithScores(studentId: UUID): List<StudentCourseWithScoresResponse> {
        val student = userRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with id $studentId not found")
        }
        
        println("DEBUG: Found student: ${student.name}, role: ${student.role}")
        
        // Check if user is a student
        if (student.role != Role.STUDENT) {
            throw IllegalArgumentException("User is not a student")
        }
          // Get active courses for student
        val courses = courseRepository.findCoursesByActiveSemestersAndStudent(student)
        println("DEBUG: Found ${courses.size} active courses for student using direct assignment")
        
        // Also try batch-based assignment
        val batchCourses = courseRepository.findCoursesByActiveSemestersAndStudentThroughBatch(student)
        println("DEBUG: Found ${batchCourses.size} active courses for student using batch assignment")
        
        // Use batch courses if direct assignment returns empty
        val finalCourses = if (courses.isEmpty() && batchCourses.isNotEmpty()) {
            println("DEBUG: Using batch-based course assignment")
            batchCourses
        } else {
            println("DEBUG: Using direct course assignment")
            courses
        }
        
        finalCourses.forEach { course ->
            println("DEBUG: Course: ${course.name} (${course.code}) - Semester active: ${course.semester.isActive}")
        }
          // For each course, get average score percentage and review count
        return finalCourses.map { course ->
            val reviewCount = individualScoreRepository.countDistinctReviewsForStudentAndCourse(student, course)
            val averageScorePercentage = if (reviewCount == 0) {
                // Default to 100% when no reviews exist yet
                100.0
            } else {
                individualScoreRepository.getAverageScorePercentageForStudentAndCourse(student, course)
            }
            
            println("DEBUG: Course ${course.name} - Reviews: $reviewCount, Average: $averageScorePercentage%")
            
            StudentCourseWithScoresResponse(
                id = course.id!!,
                name = course.name,
                code = course.code,
                description = course.description,
                averageScorePercentage = averageScorePercentage,
                reviewCount = reviewCount
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getStudentCoursePerformanceChart(studentId: UUID, courseId: UUID): List<CoursePerformanceChartResponse> {
        val student = userRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with id $studentId not found")
        }
        
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("Course with id $courseId not found")
        }
        
        // Check if user is a student
        if (student.role != Role.STUDENT) {
            throw IllegalArgumentException("User is not a student")
        }
          // Verify student is enrolled in the course (either directly or through batch)
        val isDirectlyEnrolled = course.students.contains(student)
        val batchEnrolledCourses = courseRepository.findCoursesByActiveSemestersAndStudentThroughBatch(student)
        val isEnrolledThroughBatch = batchEnrolledCourses.contains(course)
        
        if (!isDirectlyEnrolled && !isEnrolledThroughBatch) {
            throw IllegalArgumentException("Student is not enrolled in this course")
        }
        
        // Get all reviews for this course where the student has scores
        val reviews = individualScoreRepository.findDistinctReviewsByParticipantAndCourse(student, course)
        
        return reviews.map { review ->
            // Get all scores for this student in this review and course
            val scores = individualScoreRepository.findByParticipantAndReviewAndCourse(student, review, course)
            val totalPossibleScore = scores.sumOf { score -> score.criterion.maxScore.toDouble() }
            val actualScore = scores.sumOf { score -> score.score }
            val scorePercentage = if (totalPossibleScore > 0.0) {
                (actualScore / totalPossibleScore) * 100.0
            } else {
                0.0
            }
            
            // Determine status
            val currentDate = java.time.LocalDate.now()
            val status = when {
                scores.isEmpty() && review.endDate.isBefore(currentDate) -> "missed"
                scores.isNotEmpty() -> "completed"
                review.startDate.isAfter(currentDate) -> "upcoming"
                else -> "ongoing"
            }
            
            CoursePerformanceChartResponse(
                reviewId = review.id!!,
                reviewName = review.name,
                startDate = review.startDate,
                endDate = review.endDate,
                status = status,
                showResult = review.isPublished,
                score = if (scores.isNotEmpty()) actualScore else null,
                totalScore = if (scores.isNotEmpty()) totalPossibleScore else null,
                scorePercentage = if (scores.isNotEmpty()) scorePercentage else null,
                courseName = course.name,
                courseCode = course.code
            )
        }.sortedBy { it.startDate }
    }
}

fun Course.toCourseResponse(): CourseResponse {
    return CourseResponse(
        id = this.id!!,
        name = this.name,
        code = this.code,
        description = this.description
    )
}
