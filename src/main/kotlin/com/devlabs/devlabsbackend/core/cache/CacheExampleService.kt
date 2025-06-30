package com.devlabs.devlabsbackend.core.cache

import com.devlabs.devlabsbackend.core.config.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service

/**
 * Example service demonstrating Redis caching annotations
 * You can use these patterns in your actual service classes
 */
@Service
class CacheExampleService {

    /**
     * @Cacheable: Caches the result of the method call
     * If the same parameters are passed again, the cached result is returned
     */
    @Cacheable(value = [CacheConfig.USER_CACHE], key = "#userId")
    fun getUserById(userId: Long): Any? {
        // Simulate database call
        println("Fetching user from database for ID: $userId")
        return mapOf(
            "id" to userId,
            "name" to "User $userId",
            "email" to "user$userId@example.com"
        )
    }

    /**
     * @CachePut: Always executes the method and updates the cache
     * Use this for update operations
     */
    @CachePut(value = [CacheConfig.USER_CACHE], key = "#user['id']")
    fun updateUser(user: Map<String, Any>): Map<String, Any> {
        println("Updating user in database: ${user["id"]}")
        // Simulate database update
        return user
    }

    /**
     * @CacheEvict: Removes entries from the cache
     * Use this for delete operations
     */
    @CacheEvict(value = [CacheConfig.USER_CACHE], key = "#userId")
    fun deleteUser(userId: Long) {
        println("Deleting user from database: $userId")
        // Simulate database delete
    }

    /**
     * @CacheEvict with allEntries: Clears entire cache
     */
    @CacheEvict(value = [CacheConfig.USER_CACHE], allEntries = true)
    fun clearUserCache() {
        println("Clearing all user cache entries")
    }

    /**
     * @Caching: Combines multiple cache operations
     */
    @Caching(
        cacheable = [Cacheable(value = [CacheConfig.DEPARTMENT_CACHE], key = "#departmentId")],
        evict = [CacheEvict(value = [CacheConfig.USER_CACHE], allEntries = true)]
    )
    fun getDepartmentAndClearUsers(departmentId: Long): Any? {
        println("Fetching department and clearing user cache")
        return mapOf(
            "id" to departmentId,
            "name" to "Department $departmentId"
        )
    }

    /**
     * Conditional caching: Cache only if condition is met
     */
    @Cacheable(value = [CacheConfig.PROJECT_CACHE], key = "#projectId", condition = "#projectId > 0")
    fun getProjectById(projectId: Long): Any? {
        println("Fetching project from database for ID: $projectId")
        return if (projectId > 0) {
            mapOf(
                "id" to projectId,
                "name" to "Project $projectId",
                "status" to "ACTIVE"
            )
        } else null
    }

    /**
     * Cache with unless condition: Don't cache if result is null
     */
    @Cacheable(value = [CacheConfig.COURSE_CACHE], key = "#courseId", unless = "#result == null")
    fun getCourseById(courseId: Long): Any? {
        println("Fetching course from database for ID: $courseId")
        return if (courseId > 0) {
            mapOf(
                "id" to courseId,
                "name" to "Course $courseId",
                "credits" to 3
            )
        } else null
    }

    /**
     * Complex key generation using SpEL (Spring Expression Language)
     */
    @Cacheable(
        value = [CacheConfig.EVALUATION_CACHE], 
        key = "#teamId + '_' + #evaluationType + '_' + #semester"
    )
    fun getEvaluationByTeamAndType(teamId: Long, evaluationType: String, semester: String): Any? {
        println("Fetching evaluation for team: $teamId, type: $evaluationType, semester: $semester")
        return mapOf(
            "teamId" to teamId,
            "type" to evaluationType,
            "semester" to semester,
            "score" to 85.0
        )
    }
}
